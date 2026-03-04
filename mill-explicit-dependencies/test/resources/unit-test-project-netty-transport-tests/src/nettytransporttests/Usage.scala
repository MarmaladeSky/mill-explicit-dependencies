package nettytransporttests

object NettyTransportTestsUsage {
  val t = new io.netty.channel.AbstractChannelTest
  t.ensureDefaultChannelId()
  println("ok")
}
