package org.firstinspires.ftc.teamcode.helpers
import kotlin.math.abs

/**
 * Enum class that maps various linear directions
 * to an integer multiplier that can be used for
 * setting the direction of an arbitrary power.
 */
enum class Direction(val multiplier: Int) {
    FORWARDS(1),
    BACKWARDS(-1),
    STOP(0);
}

/**
 * Simple infix function that returns a number with
 * the appropriate sign as based on the Direction
 * passed to it.
 *
 * Currently only works with Float, but can be expanded
 * to other types relatively easily.
 */
infix fun Float.with(direction: Direction): Float {
    return abs(this) * direction.multiplier
}