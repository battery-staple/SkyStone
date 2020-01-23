package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode

import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector
import org.firstinspires.ftc.teamcode.helpers.*
//import java.lang.Math.*
import kotlin.math.*
import org.firstinspires.ftc.teamcode.helpers.Direction.*


@Autonomous(name = "Vision Drive", group = "Iterative Opmode")
class Autonomous : LinearOpMode(), MotorControllerOpModeHelper, DcMotorInfoOpModeHelper {

    override val opMode: OpMode = this

    private var vuforia: VuforiaLocalizer? = null

    private var tfod: TFObjectDetector? = null


    private val drivePower = 1f
    private val K = 1f
//    private val armPower = 1f
//    internal var clawPower: Float = 0f
//    internal var slidePower: Float = 0f

    //initialize directions
//    private var armDirection = Direction.STOP
//    internal var clawDirection = Direction.STOP
//    internal var slideDirection = Direction.STOP

    private val wheels = DriveablePowerBasedNonPositionalMotorController(this, WheelList(
        frontLeft = DcMotorInfo.fromName(motorName = "frontLeft", power = -1f),
        frontRight = DcMotorInfo.fromName(motorName = "frontRight", power = 1f),
        backLeft = DcMotorInfo.fromName(motorName = "backLeft", power = -1f),
        backRight = DcMotorInfo.fromName(motorName = "backRight", power = 1f)
    ))

//    private val frontLeft = PowerBasedNonPositionalMotorController(this).apply {
//        powerMultiplier = drivePower
//    }
//    private val frontRight = PowerBasedNonPositionalMotorController(this).apply {
//        powerMultiplier = drivePower
//    }
//    private val backLeft = PowerBasedNonPositionalMotorController(this).apply {
//        powerMultiplier = drivePower
//    }
//    private val backRight = PowerBasedNonPositionalMotorController(this).apply {
//        powerMultiplier = drivePower
//    }

    private val arm = PowerBasedNonPositionalMotorController(this, motors = arrayListOf(
        DcMotorInfo.fromName(motorName = "arm")
    )
    ).apply {
        direction = STOP
        defaultPower = 1f
    }
    private val claw = PowerBasedNonPositionalMotorController(this, motors = arrayListOf(
        DcMotorInfo.fromName(motorName = "intake")
    )).apply {
        direction = STOP
    }
    private val slide = PowerBasedNonPositionalMotorController(this, motors = arrayListOf(
        DcMotorInfo.fromName(motorName = "liftRight", power = -1f),
        DcMotorInfo.fromName(motorName = "liftLeft", power = 1f)
    ), brake = true)

    private fun isPressed(button: Float): Boolean {
        return button > 0.1 //change this value if necessary
    }

    override fun runOpMode() {
        telemetry.addData("Status", "Initialized")

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).

//        frontLeft.addMotor(name = "frontLeft", power = -1f)
//        frontRight.addMotor(name = "frontRight")
//        backLeft.addMotor(name = "backLeft", power = -1f)
//        backRight.addMotor(name = "backRight")
//        arm.addMotor(name = "arm")
//        claw.addMotor(name = "intake")
//        slide.addMotor(name = "liftRight", power = -1f)
//        slide.addMotor(name = "liftLeft", power = 1f)

        //Display Initialized
        telemetry.addData("Status", "Initialized")

        tfod?.shutdown()

        //Actual Autonomous
        arm.move(direction = FORWARDS, seconds = 1.0)
        wheels.driveSeconds(4.0, forward = 0.5f)
        slide.move(direction = FORWARDS, seconds = 0.1)
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private fun initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        val parameters = VuforiaLocalizer.Parameters()

        parameters.vuforiaLicenseKey = VUFORIA_KEY
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters)

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    private fun initTfod() {
        val tfodMonitorViewId = hardwareMap.appContext.resources.getIdentifier(
            "tfodMonitorViewId", "id", hardwareMap.appContext.packageName
        )
        val tfodParameters = TFObjectDetector.Parameters(tfodMonitorViewId)
        tfodParameters.minimumConfidence = 0.8
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia)
        tfod!!.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT)
    }

    private fun initTensorFlow() {
        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
        // first.
        initVuforia()

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod()
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD")
        }

        /**
         * Activate TensorFlow Object Detection before we wait for the start command.
         * Do it here so that the Camera Stream window will have the TensorFlow annotations visible.
         */

        tfod?.activate()

        /** Wait for the game to begin  */
        telemetry.addData(">", "Press Play to start op mode")
        telemetry.update()
        waitForStart()

        if (opModeIsActive()) {
//            while (opModeIsActive()) {
//                if (tfod != null) {
//                    // getUpdatedRecognitions() will return null if no new information is available since
//                    // the last time that call was made.
//                    val updatedRecognitions = tfod!!.updatedRecognitions
//                    if (updatedRecognitions != null) {
//                        telemetry.addData("# Object Detected", updatedRecognitions.size)
//
//                        // step through the list of recognitions and display boundary info.
//                        val i = 0
//                        for (recognition in updatedRecognitions) {
//                            telemetry.addData(String.format("label (%d)", i), recognition.label)
//                            telemetry.addData(
//                                String.format("  left,top (%d)", i), "%.03f , %.03f",
//                                recognition.left, recognition.top
//                            )
//                            telemetry.addData(
//                                String.format("  right,bottom (%d)", i), "%.03f , %.03f",
//                                recognition.right, recognition.bottom
//                            )
//                        }
//                        telemetry.update()
//                    }
//                }
//            }
        }
    }

    companion object {
        private const val TFOD_MODEL_ASSET = "Skystone.tflite"
        private const val LABEL_FIRST_ELEMENT = "Stone"
        private const val LABEL_SECOND_ELEMENT = "Skystone"

        private val VUFORIA_KEY =
            "AZne3+j/////AAABmW73vsKOpUy1iIrLi6QIERVkROfCZRH7Psc0Hfu51ebH5+Rwr8HmBZkBBLk/KkETR6oBhjAWkR9Qwr0KySE3niFw7Xr5zG663LsKdTQB5Yhdv3gi+Oo75YIP2kdvZU4CdlvhaCThNDPNRb5/ca4qjm45ANH6HDyKqoHpdQLo6BLBT6md3ufUmuWSILQgxxWH0W4koG8KqpDnEN2nI1p6zFx1t9lSQkju4cxMqkkt5FSTHwlowPXZK0SzC/OTiUO0lbMewL9k2abO3+RdoaFPptvfVO8IXH7hP9BV+PuX1jgdjtd7cP301XnfdwE8U/x9TENp9pyjmr+jmlE4a3r5QTNM0k0iV61CPk/3VXlQzNDL"
    }

//    private fun drive(forward: Float, right: Float = 0f, clockwise: Float = 0f) {
//
////        val forward = (-gamepad1.left_stick_y).toDouble() // push joystick1 forward to go forward
////        val right =
////            (-gamepad1.left_stick_x).toDouble() // push joystick1 to the right to strafe right
////        val clockwise =
////            (-gamepad1.right_stick_x).toDouble() // push joystick2 to the right to rotate clockwise
//
//        var front_left = forward + right + K * clockwise
//        var front_right = forward - right - K * clockwise
//        var rear_left = forward - right + K * clockwise
//        var rear_right = forward + right - K * clockwise
//
//
//
//        val max = Collections.max(
//            listOf(front_left, front_right, rear_left, rear_right)
//                .map(::abs)
//        ) // gets the largest absolute value of the direction powers
//
//        if (max > 1) {
//            front_left /= max
//            front_right /= max
//            rear_left /= max
//            rear_right /= max
//        }
//
//        frontLeft.power = front_left
//        frontRight.power = front_right
//        backRight.power = rear_right
//        backLeft.power = rear_left
//    }

//    private fun stopDriving() = drive(0f)

//    private fun driveSeconds(seconds: Long, forward: Float = 0f, right: Float = 0f, clockwise: Float = 0f) {
//        drive(forward, right, clockwise)
//        waitSeconds(seconds)
//        stopDriving()
//    }

//    private fun grab() {
//        //        switch (armDirection) {
//        //            case FORWARDS:
//        //                arm.setTargetPosition(0);
//        //                arm.setPower(armPower);
//        //                break;
//        //            case BACKWARDS:
//        //                arm.setTargetPosition(-2000);
//        //                arm.setPower(armPower);
//        //                break;
//        //            case STOP:
//        //                arm.setPower(0);
//        //                break;
//        //        }
//
//        arm!!.power = (armDirection.multiplier * armPower).toDouble()
//        claw!!.power = (clawDirection.multiplier * clawPower).toDouble()
//        moveSlide((slideDirection.multiplier * slidePower).toDouble())
//    }

//    private fun waitSeconds(time: Double) {
//        resetStartTime()
//        while (runtime < time && opModeIsActive());
//        return
//    }

//    private fun NonPositionalMotorController.moveSeconds(time: Double, power: Float = 0f, direction: Direction = Direction.STOP) {
//        this.direction = direction
//        this.power = abs(power)
//        waitSeconds(time)
//        this.direction = Direction.STOP
//    }

//    private fun moveSlide(power: Double) {
//        liftLeft!!.setPower(power)
//        liftRight!!.setPower(-power)
//    }
}

