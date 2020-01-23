package org.firstinspires.ftc.teamcode.helpers

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotor.RunMode.*

interface OpModeHelper {
    /**
     * Many helpers require access to the opMode for
     * things like telemetry and the hardwareMap.
     * In almost every scenario, this should be overriden
     * with "this".
     */
    val opMode: OpMode
}

interface DcMotorInfoOpModeHelper: OpModeHelper {
    fun DcMotorInfo.Factory.fromMotor(motor: DcMotor, power: Float = 1f) =
        fromMotor(opMode.hardwareMap, motor, power)

    fun DcMotorInfo.Factory.fromName(motorName: String, power: Float = 1f) =
        fromName(opMode.hardwareMap, motorName, power)
}

interface MotorControllerOpModeHelper: OpModeHelper {
    fun createMotorController(
        runMode: DcMotor.RunMode, motors: HashSet<DcMotorInfo>,
        powerMultiplier: Float, brake: Boolean? = true): MotorController = when(runMode) {
        RUN_TO_POSITION -> PositionalMotorController(opMode, motors, powerMultiplier, brake)
        RUN_USING_ENCODER -> SpeedBasedNonPositionalMotorController(opMode, motors, powerMultiplier, brake)
        RUN_WITHOUT_ENCODER -> PowerBasedNonPositionalMotorController(opMode, motors, powerMultiplier, brake)
        STOP_AND_RESET_ENCODER -> run {
            for (motor in motors) { motor.encoderMode = STOP_AND_RESET_ENCODER }
            return@run createMotorController(runMode, motors, powerMultiplier, brake)
        }
        RESET_ENCODERS, RUN_WITHOUT_ENCODERS, RUN_USING_ENCODERS ->
            createMotorController(runMode.migrate(), motors, powerMultiplier, brake)
    }
}

interface Waitable : OpModeHelper {
    fun waitSeconds(seconds: Double) {
        opMode.run {
            if (this is LinearOpMode) {
                resetStartTime()
                while (runtime < seconds && opModeIsActive()) {}
            }
        }
    }
}