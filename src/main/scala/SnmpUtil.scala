import org.snmp4j.event.ResponseEvent
import org.snmp4j.{CommunityTarget, PDU, Snmp}
import org.snmp4j.mp.SnmpConstants
import org.snmp4j.smi._
import org.snmp4j.transport.DefaultUdpTransportMapping

/**
  * Created by larryf on 8/18/2016.
  */
object SnmpUtil {

//  val ipAddress: String = "10.40.99.127"
  val ipAddress: String = "FE80::725A:FFF:FEBC:3829"
//  val ipAddress: String = "FE80::725A:FFF:FEBC:382A"

  val port: String = "161"
  val oidValue: String = "1.3.6.1.2.1.43.10.2.1.4.1.1"

  val snmpVersion  = SnmpConstants.version2c
  val community: String = "public"

  def main(args: Array[String]): Unit = {
    val transport = new DefaultUdpTransportMapping()
    transport.listen()

    // Create Snmp object for sending data to Agent
    val snmp = new Snmp(transport)
    try {

      // Create Target Address object
      val comtarget: CommunityTarget = new CommunityTarget()
      comtarget.setCommunity(new OctetString(community))
      comtarget.setVersion(snmpVersion)
      comtarget.setAddress(new UdpAddress(s"$ipAddress/$port"))
      comtarget.setRetries(2)
      comtarget.setTimeout(1000)

      // Create the PDU objec
      val pdu = new PDU()
      pdu.add(new VariableBinding(new OID(oidValue)))
      pdu.setType(PDU.GET)
      pdu.setRequestID(new Integer32(1))

      println("Sending Request to Agent...")
      val response: Option[ResponseEvent] = Option(snmp.get(pdu, comtarget))
      // Process Agent Response
      response match {
        case Some(r) =>
          println("Got Response from Agent")
          Option(r.getResponse) match {
            case Some(pdu) =>
              pdu.getErrorStatus match {
                case PDU.noError =>
                  println(s"Snmp Get Response = ${pdu.getVariableBindings}")
                case _ =>
                  println("Error: Request Failed")
                  println(s"Error Status = ${pdu.getErrorStatus}")
                  println(s"Error Index = ${pdu.getErrorIndex}")
                  println(s"Error Status Text = ${pdu.getErrorStatusText}")
              }
            case None =>
              println("Error: Response PDU is None")
          }
        case None =>
          println("Error: Agent Timeout... ")
      }
    } catch {
      case e: Exception =>
        println(e.toString)
    }
    snmp.close()
  }
}
