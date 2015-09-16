package irc.server



object IrcServerCreator {

  @throws(classOf[IllegalArgumentException])
  def create(name: String, address: String, port: Int, useSSL: Boolean): IrcServer= {
    if(port < 1 || port > 65535) throw new IllegalArgumentException("Port is not between 1 and 65535")
    new IrcServer(name, address, port, useSSL)
  }
}