package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.helpers.Direction;
//import org.firstinspires.ftc.teamcode.helpers.MC;
//import org.firstinspires.ftc.teamcode.helpers.MotorController;
import org.jetbrains.annotations.Contract;


import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@TeleOp(name = "Ultron drive", group = "Iterative Opmode")

public class ChildDrive extends OpMode {

    private float drivePower = 1;
    private float K = 1;
    private float armPower = 1;
    float clawPower;
    float slidePower;

    //initialize directions
    private Direction armDirection = Direction.STOP;
    Direction clawDirection = Direction.STOP;
    Direction slideDirection = Direction.STOP;

    private boolean isPressed(float button) {
        return button > 0.1; //change this value if necessary
    }

//    private MotorController frontLeft = null;
//    private MotorController frontRight = null;
//    private MotorController backLeft = null;
//    private MotorController backRight = null;
//    private MotorController arm = null;
//    private MotorController intake = null;
//    private MotorController liftRight = null;
//    private MotorController liftLeft = null;

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private DcMotor arm;
    private DcMotor intake;
    private DcMotor liftRight;
    private DcMotor liftLeft;

//    private Servo armServo;

    @Override
    public void init() {
        telemetry.addData("Status", "Initialized");

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");
        arm = hardwareMap.get(DcMotor.class, "arm");
        intake = hardwareMap.get(DcMotor.class, "intake");
        liftRight = hardwareMap.get(DcMotor.class, "liftRight");
        liftLeft = hardwareMap.get(DcMotor.class, "liftLeft");

//        armServo = hardwareMap.get(Servo.class, "armServo");

//        armServo.setPosition(0.0);

        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);

        liftLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //Display Initialized
        telemetry.addData("Status", "Initialized");


//        arm.setTargetPosition(0);
        arm.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//        arm.setPower(0.7);
//        telemetry.addData(String.valueOf(arm.getTargetPosition()), "");
    }


    @Override
    public void loop() {
        telemetry.addData("Yeet", arm.getCurrentPosition());

        //----precision drive mode----//
        if (isPressed(gamepad1.left_trigger)) {
            drivePower = 0.3f;
//            K = 0.5f;
        } else {
            drivePower = 1.0f;
//            K = 1.0f;
        }

        //----precision arm mode----//
//        armPower = gamepad1.right_trigger;
        if (isPressed(gamepad1.right_trigger)) {
            // may be too weak
            armPower = 0.3f;
        } else {
            armPower = 1.0f;
        }

        //----arm----//
        if (isPressed(gamepad1.a)) {
            //raise
            armDirection = Direction.FORWARDS;
        } else if (isPressed(gamepad1.b)) {
            //lower
            armDirection = Direction.BACKWARDS;
        } else {
            //nothing
            armDirection = Direction.STOP;
        }

        //----slide----//
        if (isPressed(gamepad1.dpad_up)) {
            //raise
            slideDirection = Direction.FORWARDS;
            slidePower = 0.7f;
        } else if (isPressed(gamepad1.dpad_down)) {
            //lower
            slideDirection = Direction.BACKWARDS;
            slidePower = 0.7f;
        } else {
            //nothing
            slideDirection = Direction.STOP;
        }

        //----claw----//
        if (isPressed(gamepad1.left_bumper)) {
            //close
            clawPower = 0.5f;
            clawDirection = Direction.FORWARDS;
        } else if (isPressed(gamepad1.right_bumper)) {
            //open
            clawPower = 0.5f;
            clawDirection = Direction.BACKWARDS;
        } else {
            //if currently closing, keep closing with less power,
            //otherwise, stop.
            if (clawDirection == Direction.FORWARDS && !isPressed(gamepad1.x)) {
//            if (true) {
                clawPower = 0.2f;
            } else {
                clawDirection = Direction.STOP;
            }
        }

//        //----spinny claw part----//
//        if (isPressed(gamepad1.y)) {
//            double position = armServo.getPosition();
//            if (position >= 0.4) {
//                armServo.setPosition(0.0);
//            } else {
//                armServo.setPosition(0.5);
//            }
//        }

        drive();
        grab();
    }

    private void drive() {

        double forward = -gamepad1.left_stick_y; // push joystick1 forward to go forward
        double right = -gamepad1.left_stick_x; // push joystick1 to the right to strafe right
        double clockwise = -gamepad1.right_stick_x; // push joystick2 to the right to rotate clockwise

        double front_left = forward + right + (K * clockwise);
        double front_right = forward - right - (K * clockwise);
        double rear_left = forward - right + (K * clockwise);
        double rear_right = forward + right - (K * clockwise);

        double max = Collections.max(Arrays.asList(
                Math.abs(front_left),
                Math.abs(front_right),
                Math.abs(rear_left),
                Math.abs(rear_right)//,
        )); // gets the largest absolute value of the direction powers

        if (max > 1) {
            front_left /= max;
            front_right /= max;
            rear_left /= max;
            rear_right /= max;
        }

        frontLeft.setPower(front_left * drivePower);
        frontRight.setPower(front_right * drivePower);
        backRight.setPower(rear_right * drivePower);
        backLeft.setPower(rear_left * drivePower);

    }

    private void grab() {
//        switch (armDirection) {
//            case FORWARDS:
//                arm.setTargetPosition(0);
//                arm.setPower(armPower);
//                break;
//            case BACKWARDS:
//                arm.setTargetPosition(-2000);
//                arm.setPower(armPower);
//                break;
//            case STOP:
//                arm.setPower(0);
//                break;
//        }

        arm.setPower(armDirection.getMultiplier() * armPower);
        intake.setPower(clawDirection.getMultiplier() * clawPower);
        moveSlide(slideDirection.getMultiplier() * slidePower);
    }

    @Contract(value = "true -> true; false -> false", pure = true)
    private boolean isPressed(boolean button) {
        return button;
    }

    private void moveSlide(double power) {
        liftLeft.setPower(power);
        liftRight.setPower(-power);
    }
}
/*

package org.firstinspires.ftc.teamcode;

        import com.qualcomm.robotcore.eventloop.opmode.OpMode;
        import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
        import com.qualcomm.robotcore.hardware.CRServo;
        import com.qualcomm.robotcore.hardware.DcMotor;
        import com.qualcomm.robotcore.hardware.DcMotorSimple;

        import org.firstinspires.ftc.teamcode.helpers.Direction;
        import org.firstinspires.ftc.teamcode.helpers.MotorController;

        import java.util.Collections;
        import java.util.Arrays;


@TeleOp(name = "Ultron drive", group = "Iterative Opmode")

public class ChildDrive extends OpMode {

    private float drivePower = 1;
    private float K = 1;
    private float armPower = 1;
    float clawPower;
    float slidePower;

    //initialize directions
    private Direction armDirection = Direction.STOP;
    Direction clawDirection = Direction.STOP;
    Direction slideDirection = Direction.STOP;

    private boolean isPressed(float button) {
        return button > 0.1; //change this value if necessary
    }

    private DcMotor frontLeft = null;
    private DcMotor frontRight = null;
    private DcMotor backLeft = null;
    private DcMotor backRight = null;
    private DcMotor arm = null;
    private DcMotor intake = null;
    private DcMotor liftRight = null;
    private DcMotor liftLeft = null;

    @Override
    public void init() {
        telemetry.addData("Status", "Initialized");

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");
        arm = hardwareMap.get(DcMotor.class, "arm");
        intake = hardwareMap.get(DcMotor.class, "intake");
        liftRight = hardwareMap.get(DcMotor.class, "liftRight");
        liftLeft = hardwareMap.get(DcMotor.class, "liftLeft");
        frontLeft.setDirection(DcMotor.Direction.REVERSE);
        backLeft.setDirection(DcMotor.Direction.REVERSE);

        liftLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //Display Initialized
        telemetry.addData("Status", "Initialized");


        arm.setTargetPosition(-1);
        arm.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        arm.setPower(0.7);
        telemetry.addData(String.valueOf(arm.getTargetPosition()), "");
    }


    @Override
    public void loop() {
        telemetry.addData("Yeet", arm.getCurrentPosition());

        //----precision drive mode----//
        {
            if (isPressed(gamepad1.left_trigger)) {
                drivePower = 0.5f;
                //            K = 0.5f;
            } else {
                drivePower = 1.0f;
                //            K = 1.0f;
            }
        }

        //----precision arm mode----//
//        armPower = gamepad1.right_trigger;
        if (isPressed(gamepad1.right_trigger)) {
            // may be too weak
            armPower = 0.3f;
        } else {
            armPower = 1.0f;
        }

        //----arm----//
        if (isPressed(gamepad1.a)) {
            //raise
            armDirection = Direction.FORWARDS;
        } else if (isPressed(gamepad1.b)) {
            //lower
            armDirection = Direction.BACKWARDS;
        } else {
            //nothing
            armDirection = Direction.STOP;
        }

        //----slide----//
        if (isPressed(gamepad1.dpad_up)) {
            //raise
            slideDirection = Direction.FORWARDS;
            slidePower = 0.7f;
        } else if (isPressed(gamepad1.dpad_down)) {
            //lower
            slideDirection = Direction.BACKWARDS;
            slidePower = 0.7f;
        } else {
            //nothing
            slideDirection = Direction.STOP;
        }

        //----claw----//
        if (isPressed(gamepad1.left_bumper)) {
            //close
            clawPower = 0.5f;
            clawDirection = Direction.FORWARDS;
        } else if (isPressed(gamepad1.right_bumper)) {
            //open
            clawPower = 0.5f;
            clawDirection = Direction.BACKWARDS;
        } else {
            //if currently closing, keep closing with less power,
            //otherwise, stop.
            if (clawDirection == Direction.FORWARDS && !isPressed(gamepad1.x)) {
//            if (true) {
                clawPower = 0.1f;
            } else {
                clawDirection = Direction.STOP;
            }
        }

        drive();
        grab();
    }

    private void drive() {

        double forward = -gamepad1.left_stick_y; // push joystick1 forward to go forward
        double right = -gamepad1.left_stick_x; // push joystick1 to the right to strafe right
        double clockwise = -gamepad1.right_stick_x; // push joystick2 to the right to rotate clockwise

        double front_left = forward + right + (K * clockwise);
        double front_right = forward - right - (K * clockwise);
        double rear_left = forward - right + (K * clockwise);
        double rear_right = forward + right - (K * clockwise);

        */
/*double max = Math.abs(front_left);
        if (Math.abs(front_right) > max) max = Math.abs(front_right);
        if (Math.abs(rear_left) > max) max = Math.abs(rear_left);
        if (Math.abs(rear_right) > max) max = Math.abs(rear_right);
        // couldn't all this be replaced with max(front_left,...)? Currently, this is O(n)...*//*


        double max = Collections.max(Arrays.asList(
                Math.abs(front_left),
                Math.abs(front_right),
                Math.abs(rear_left),
                Math.abs(rear_right)//,
        )); // gets the largest absolute value of the direction powers

        if (max > 1) {
            front_left /= max;
            front_right /= max;
            rear_left /= max;
            rear_right /= max;
        }

        frontLeft.setPower(front_left * drivePower);
        frontRight.setPower(front_right * drivePower);
        backRight.setPower(rear_right * drivePower);
        backLeft.setPower(rear_left * drivePower);

    }

    private void grab() {
        switch (armDirection) {
            case FORWARDS:
                arm.setTargetPosition(0);
                arm.setPower(armPower);
                break;
            case BACKWARDS:
                arm.setTargetPosition(-2000);
                arm.setPower(armPower);
                break;
            case STOP:
                arm.setPower(0);
                break;
        }
        intake.setPower(clawDirection.multiplier * clawPower);
        moveSlide(slideDirection.multiplier * slidePower);

    }

    private boolean isPressed(boolean button) {
        return button;
    }
    private void moveSlide(double power) {
        liftLeft.setPower(power);
        liftRight.setPower(-power);
    }
}
*/
