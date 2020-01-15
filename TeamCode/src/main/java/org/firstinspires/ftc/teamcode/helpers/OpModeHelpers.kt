//@file:JvmMultifileClass // TODO: Valid?
//package org.firstinspires.ftc.teamcode.helpers
//
//import com.qualcomm.robotcore.eventloop.opmode.OpMode
//import com.qualcomm.robotcore.hardware.DcMotor
////import org.firstinspires.ftc.teamcode.helpers.MotorController
//import com.qualcomm.robotcore.hardware.HardwareMap
//import java.lang.IllegalArgumentException
//
//interface OpModeHelpers {
//    /**
//     * Many helpers require access to the opMode for
//     * things like telemetry and the hardwareMap.
//     * In almost every scenario, this should be overriden
//     * with "this".
//     */
//    val opMode: OpMode
//}
//
//interface DcMotorInfoOpModeHelpers: OpModeHelpers {
//    fun DcMotorInfo.Factory.fromMotor(motor: DcMotor, power: Float = 1f) =
//        fromMotor(opMode.hardwareMap, motor, power)
//
//    fun DcMotorInfo.Factory.fromName(motorName: String, power: Float = 1f) =
//        fromName(opMode.hardwareMap, motorName, power)
//}
//
//interface MotorControllerOpModeHelpers: OpModeHelpers {
//
////    fun MotorController(motors: HashSet<DcMotorInfo>, powerMultiplier: Float = 1f): MotorController {
////        return (opMode, motors, powerMultiplier)
////    }
//}