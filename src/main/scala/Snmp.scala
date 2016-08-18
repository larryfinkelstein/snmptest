import org.snmp4j.event.ResponseEvent
import org.snmp4j.{CommunityTarget, PDU, Snmp}
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi._
import org.snmp4j.transport.DefaultUdpTransportMapping

/**
  * Created by larryf on 8/18/2016.
  */
object Snmp {

  val ipAddress: String = "10.40.99.127"
  // FE80::725A:FFF:FEBC:3829

  val port: String = "161"
  val oidValue: String = "1.3.6.1.2.1.43.10.2.1.4.1.1"

  val snmpVersion  = SnmpConstants.version2c
  val community: String = "public"

  def main(args: Array[String]): Unit = {
    val transport = new DefaultUdpTransportMapping()
    transport.listen()

    // Create Target Address object
    val comtarget: CommunityTarget = new CommunityTarget()
    comtarget.setCommunity(new OctetString(community))
    comtarget.setVersion(snmpVersion)
    comtarget.setAddress(new UdpAddress(s"${ipAddress}/${port}"))
    comtarget.setRetries(2)
    comtarget.setTimeout(1000)

    // Create the PDU objec
    val pdu = new PDU()
    pdu.add(new VariableBinding(new OID(oidValue)))
    pdu.setType(PDU.GET)
    pdu.setRequestID(new Integer32(1))

    // Create Snmp object for sending data to Agent
    val snmp = new Snmp(transport)

    println("Sending Request to Agent...")
    val response: ResponseEvent = snmp.get(pdu, comtarget)
    // Process Agent Response
    if (response != null)
    {
      println("Got Response from Agent")
      val responsePDU:PDU = response.getResponse()

      if (responsePDU != null)
      {
        val errorStatus: Int = responsePDU.getErrorStatus()
        val errorIndex: Int = responsePDU.getErrorIndex()
        val errorStatusText: String = responsePDU.getErrorStatusText()

        if (errorStatus == PDU.noError)
        {
          println("Snmp Get Response = " + responsePDU.getVariableBindings())
        }
        else
        {
          println("Error: Request Failed")
          println("Error Status = " + errorStatus)
          println("Error Index = " + errorIndex)
          println("Error Status Text = " + errorStatusText)
        }
      }
      else
      {
        println("Error: Response PDU is null")
      }
    }
    else
    {
      println("Error: Agent Timeout... ")
    }
    snmp.close()
  }
}
