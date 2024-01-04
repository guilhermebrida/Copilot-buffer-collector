package br.com.crearesistemas.communicator


import com.fazecast.jSerialComm.SerialPort
import org.hibernate.internal.util.BytesHelper
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement


val url = "jdbc:sqlite:C:/Kotlin-scripts/database.db"
//var connection: Connection? = null



fun generateAck(id : String, sequence : Int) : String{
    val seq = "%04X".format(sequence)
    var resp = ">ACK;ID=$id;#$seq;*"
    resp += String.format("%02X", calcCheckSum(resp))
    return resp
}


data class XVMData(val message: String, val id: String, val sequence: Int, val checksum: Int)
fun parserXVM(msg : String) : XVMData {
    val xvmMessage = msg.split(";")
    val message = xvmMessage[0]
    val id  = xvmMessage[1].substringAfter("=")
    val sequence =  xvmMessage[2].substringAfter("#").toInt()
    val checksum = xvmMessage[3].substringAfter("*").toInt()
    return XVMData(message,id,sequence,checksum)
}


fun calcCheckSum(message: String) : Int{
    val num = message.indexOf(";*") + 1
    var calc = 0

    for (i in 0 until num) {
        calc = calc xor message[i].toInt()
    }
    println(calc)
    return calc
}


fun generateXVM (id : String,sequence : Int, message : String) : String{
    var resp =  "$message;ID=$id;#$sequence;*"
    resp += String.format("%02X", calcCheckSum(resp)) + "\r\n"
    return resp
}


fun sendCommands(portaSerial : SerialPort, deviceID : String, command : String): String {
    for (i in 0 until 5) {
        try {
            val xvm = generateXVM(deviceID, 1000 + i, command)
            println(xvm)
            portaSerial.writeBytes(xvm.toByteArray(Charsets.UTF_8), xvm.length.toLong())
            Thread.sleep(1000)
            //        val buffer = ByteArray(1024)
            //        val res = portaSerial.readBytes(buffer, buffer.size.toLong())
//            val input = portaSerial.inputStream
//            val reader = BufferedReader(InputStreamReader(input))
//            val res: String = reader.readLine()
            val res = receiveCommands(portaSerial)
            println("resposta na sendcommands$res")
            if (res != "") return res
            if (i == 4) {
                throw Exception()
            }

        } catch (e: Exception) {
            throw Exception("FALHA COMUNICAÇÃO")
        }
    }
    return ""
}

fun receiveCommands (portaSerial: SerialPort): String {
    val input = portaSerial.inputStream
    val reader = BufferedReader(InputStreamReader(input))
    val res: String = reader.readLine()
    if (res != "") return res
    return ""
}


fun getDeviceID(portaSerial: SerialPort): String {
    val res = sendCommands(portaSerial, "1234", ">QVR<")
    val id = res.split(";")[1].substringAfter("=")
    if (id != "1234") {
        println(id)
        return id
    }
    return ""
}

fun getMessages(portaSerial: SerialPort, deviceID: String) {
//    val resposta = sendCommands(portaSerial, deviceID, ">QUV00rTRM<")
    val xvm = generateXVM(deviceID, 1010, ">QUV00rTRM<")
    println(xvm)
    portaSerial.writeBytes(xvm.toByteArray(Charsets.UTF_8), xvm.length.toLong())
    Thread.sleep(1000)
    while (true) {
        println("entrou no while")
//        val resposta = receiveCommands(portaSerial)
        val input = portaSerial.inputStream
        val reader = BufferedReader(InputStreamReader(input))
        val res: String = reader.readLine()
        Thread.sleep(1000)
        if (res == "") {
            println("acabou")
//            break
//    } else {
//        val (message, id, sequence, checksum) = parserXVM(resposta)
//        val ack = generateAck(id, sequence)
//        portaSerial.writeBytes(ack.toByteArray(Charsets.UTF_8), ack.length.toLong())

        }
    }


}


fun main() {
//    val url = "jdbc:postgresql://localhost:5432/inbox"
//    val username = "postgres"
//    val password = "postgres"
//    Database.connect(url, driver = "org.postgresql.Driver", user = username, password = password)
//        var connection = DriverManager.getConnection(url)
//        if (connection != null) {
//            val statement: Statement = connection.createStatement()
//            var sql = """
//            CREATE TABLE IF NOT EXISTS coleta (
//                id INTEGER PRIMARY KEY AUTOINCREMENT,
//                device_id TEXT,
//                message TEXT
//            )
//        """
//            statement.executeUpdate(sql)
//        }





    val portasSeriais = SerialPort.getCommPorts()

    if (portasSeriais.isEmpty()) {
        println("Nenhuma porta serial encontrada.")
        return
    }

    println("Portas Seriais Disponíveis:")
    for (porta in portasSeriais) {
        println(porta.systemPortName)
    }

    val portaSerial = SerialPort.getCommPort("COM11")


    portaSerial.setBaudRate(19200)
    portaSerial.setNumDataBits(8)
    portaSerial.setParity(SerialPort.NO_PARITY)
    portaSerial.setNumStopBits(1)


    if (portaSerial.openPort()) {
        println("Porta serial aberta com sucesso.")
        val deviceID: String = getDeviceID(portaSerial)
        println(deviceID)
        sendCommands(portaSerial, deviceID, ">TCFG53,2<")
        sendCommands(portaSerial, deviceID, ">VSIP0,TRM1<")
        getMessages(portaSerial, deviceID)


    } else {
        println("Falha ao abrir a porta serial.")
    }
}

