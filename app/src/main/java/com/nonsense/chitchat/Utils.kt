package com.nonsense.chitchat

import java.util.regex.Matcher
import java.util.regex.Pattern

fun ipCheck(ip: String): Boolean {
    val ipPattern = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})")
    val matcher = ipPattern.matcher(ip)
    return matcher.matches()
}

fun portCheck(port: String): Boolean {
    val portPattern = Pattern.compile("(\\d{4,5})")
    val matcher = portPattern.matcher(port)
    return matcher.matches()
}


data class IPAddress(private val binaryValue: Int) : Comparable<IPAddress> {
    override fun compareTo(other: IPAddress): Int =
        Integer.compareUnsigned(binaryValue, other.binaryValue)

    constructor(ip: String) : this(parseIP(ip))

    companion object {
        private const val IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})"
        private val ipPattern = Pattern.compile(IP_ADDRESS)

        private fun parseIP(ip: String): Int {
            val matcher = ipPattern.matcher(ip)
            require(matcher.matches()) { "Could not parse [$ip]" }

            return matcher.matchAddress()
        }
    }
}

data class IPAddressRange constructor(private val range: ClosedRange<IPAddress>) :
    ClosedRange<IPAddress> by range {

    constructor(cidr: String) : this(parseCIDR(cidr))

    companion object {
        private const val IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})"
        private const val SLASH_FORMAT = "$IP_ADDRESS/(\\d{1,3})"
        private val cidrPattern = Pattern.compile(SLASH_FORMAT)

        private fun parseCIDR(cidr: String): ClosedRange<IPAddress> {
            val matcher = cidrPattern.matcher(cidr)
            require(matcher.matches()) { "Could not parse [$cidr]" }

            val address = matcher.matchAddress()
            /* Create a binary netmask from the number of bits specification /x */
            val cidrPart: Int = matcher.group(5).toInt().mustBeInRange(IntRange(0, 32))

            val netmask = cidrMasks[cidrPart]
            val network = address and netmask
            return IPAddress(network)..IPAddress(address or netmask.inv())
        }

        private val cidrMasks = 32.calculateBitMasks().toIntArray()

        private fun Int.calculateBitMasks(): List<Int> {
            var current = 0L
            return (0..this).map { bit ->
                current += if (bit == 0) 0
                else 1L shl bit - 1
                current.inv().toInt()
            }.reversed()
        }
    }
}

/**
 * Convenience method to extract the components of a dotted decimal address and
 * pack into an integer using a regex match
 */
fun Matcher.matchAddress(): Int {
    var addr = 0
    for (i in 1..4) {
        val n: Int = this.group(i).toInt().mustBeInRange(0..255)
        addr = addr or (n and 0xff shl 8 * (4 - i))
    }
    return addr
}

/**
 * Convenience function to check integer boundaries.
 * Checks if a value x is in the range [begin,end].
 * Returns x if it is in range, throws an exception otherwise.
 */
fun Int.mustBeInRange(range: IntRange): Int {
    require(range.contains(this)) { "Value $this not in range $range" }
    return this
}