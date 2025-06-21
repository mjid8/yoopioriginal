package com.yoopi.player.net

import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress

/** Forces OkHttp / ExoPlayer to use IPv4 even when the OS prefers IPv6. */
class Ipv4OnlyDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> =
        InetAddress.getAllByName(hostname)
            .filterIsInstance<Inet4Address>()      // drop any v6 addresses (real or NAT64)
}
