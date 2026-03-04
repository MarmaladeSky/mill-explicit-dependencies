package nettyepoll

import io.netty.channel.epoll.Epoll

object EpollServer {
  def isNativeTransportAvailable: Boolean = Epoll.isAvailable
}
