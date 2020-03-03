/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;





import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Timer;

//Things that I added. They might be useful
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.networktables.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  private boolean m_LimelightHasValidTarget = false;
	private double m_LimelightDriveCommand = 0.0;
	private double m_LimelightSteerCommand = 0.0;
  
  // Drivetrain
  SpeedControllerGroup left;
  SpeedControllerGroup right;

  Joystick joy1;
  Joystick joy2;

  // Intake
  SpeedControllerGroup intake;

  // Shooter
  SpeedControllerGroup shooter;

  Timer autoTimer;

  Solenoid drive_right;
  Solenoid drive_left;

  DoubleSolenoid climb_right = new DoubleSolenoid(2,0,1);
  DoubleSolenoid climb_left;
  DoubleSolenoid intakes;

  NetworkTableEntry tx;
	NetworkTableEntry ty;
	NetworkTableEntry ta;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    CANSparkMax left1 = new CANSparkMax(1, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax left2 = new CANSparkMax(2, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax left3 = new CANSparkMax(3, CANSparkMaxLowLevel.MotorType.kBrushless);

    CANSparkMax right1 = new CANSparkMax(5, CANSparkMaxLowLevel.MotorType.kBrushless);
    //CANSparkMax right2 = new CANSparkMax(6, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax right3 = new CANSparkMax(7, CANSparkMaxLowLevel.MotorType.kBrushless);

    left = new SpeedControllerGroup(left1, left2, left3);
    right = new SpeedControllerGroup(right1, /*right2*/ right3);

    right.setInverted(true);
    
    //TODO make sure intake is in PWM port 0
    Spark intakePWM1 = new Spark(0);

    Spark intakePWM2 = new Spark(1);
    intakePWM2.setInverted(true);
    intake = new SpeedControllerGroup(intakePWM1, intakePWM2);
  
    Spark shooter1 = new Spark(1);
    Spark shooter2 = new Spark(2);

    shooter = new SpeedControllerGroup(shooter1, shooter2);


    joy1 = new Joystick(0);
    joy2 = new Joystick(1);

    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
		tx = table.getEntry("tx");
		ty = table.getEntry("ty");
		ta = table.getEntry("ta");
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    autoTimer.reset();
    autoTimer.start();
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }

    if(autoTimer.get() < 1){
      left.set(.5);
    }
    else if(autoTimer.get() < 5){
      left.set(1);
      right.set(1);
    }
    else if(autoTimer.get() < 6){
      right.set(.5);
    }
    else if(autoTimer.get() < 10){
      shooter.set(1);
      intake.set(1);
    }
    else if(autoTimer.get() < 11){
      right.set(.2);
    }
    else if(autoTimer.get() < 15){
      right.set(1);
      left.set(1);
      shooter.set(1);
      intake.set(1);
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    double leftSpeed = -joy1.getRawAxis(3);
    double leftMod = leftSpeed*leftSpeed*leftSpeed;
    double rightSpeed = -joy1.getRawAxis(5);
    double rightMod = rightSpeed*rightSpeed*rightSpeed;

    if(joy1.getRawButton(3) && m_LimelightHasValidTarget){
      drive(m_LimelightDriveCommand, m_LimelightSteerCommand);
    }
    else{
      drive(leftMod, rightMod);
    }

    double intakeSpeed = joy1.getRawAxis(6);

    intake.set(intakeSpeed);

    double shooterSpeed =joy1.getRawAxis(/*right trigger*/0);

    shooter.set(shooterSpeed);
  }
  public void Update_Limelight_Tracking()
  {
  	// These numbers must be tuned for your Robot!  Be careful!
  	final double STEER_K = 0.02;                    // how hard to turn toward the target
  	final double DRIVE_K = 0.05;                    // how hard to drive fwd toward the target
  	final double DESIRED_TARGET_AREA = 13.0;        // Area of the target when the robot reaches the wall
  	final double MAX_DRIVE = 0.10;                   // Simple speed limit so we don't drive too fast

  	double tv = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tv").getDouble(0);
  	double tx = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);
  	double ty = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ty").getDouble(0);
  	double ta = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ta").getDouble(0);

  	if (tv < 1.0)
  	{
  	  m_LimelightHasValidTarget = false;
  	  m_LimelightDriveCommand = 0.0;
  	  m_LimelightSteerCommand = 0.0;
  	  return;
  	}

  	m_LimelightHasValidTarget = true;

  	// Start with proportional steering
  	double steer_cmd = tx * STEER_K;
  	m_LimelightSteerCommand = steer_cmd;

  	// try to drive forward until the target area reaches our desired area
  	double drive_cmd = (DESIRED_TARGET_AREA - ta) * DRIVE_K;

  	// don't let the robot drive too fast into the goal
  	if (drive_cmd > MAX_DRIVE)
  	{
  	  drive_cmd = MAX_DRIVE;
  	}
    m_LimelightDriveCommand = drive_cmd;
  }
  public void drive(double l, double r){
		//double speedModifier = .8;
		left.set(l/**speedModifier*/);
		right.set(r/**speedModifier*/);
	}

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
