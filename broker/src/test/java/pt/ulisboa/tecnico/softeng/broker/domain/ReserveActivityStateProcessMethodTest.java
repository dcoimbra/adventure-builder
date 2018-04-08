package pt.ulisboa.tecnico.softeng.broker.domain;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import pt.ulisboa.tecnico.softeng.activity.exception.ActivityException;
import pt.ulisboa.tecnico.softeng.broker.domain.Adventure.State;
import pt.ulisboa.tecnico.softeng.broker.exception.RemoteAccessException;
import pt.ulisboa.tecnico.softeng.broker.interfaces.ActivityInterface;

@RunWith(JMockit.class)
public class ReserveActivityStateProcessMethodTest {
	private static final String IBAN = "BK01987654321";
	private static final String NIF = "123456789";
	private static final double AMOUNT = 0.3;
	private static final int AGE = 20;
	private static final String ACTIVITY_CONFIRMATION = "ActivityConfirmation";
	private static final LocalDate begin = new LocalDate(2016, 12, 19);
	private static final LocalDate end = new LocalDate(2016, 12, 21);
	private Adventure adventure;
	private static final String DRIVING_LICENSE = "IMT1234";



	@Injectable
	private Broker broker;

	private Client client;

	@Before
	public void setUp() {
		this.client = new Client(broker, IBAN, NIF, DRIVING_LICENSE ,AGE);
		this.adventure = new Adventure(this.broker, begin, end, this.client, AMOUNT, true);
		this.adventure.setState(State.RESERVE_ACTIVITY);
	}

	@Test
	public void successNoBookRoom(@Mocked final ActivityInterface activityInterface) {
		Adventure sameDayAdventure = new Adventure(this.broker, begin, begin, this.client, AMOUNT, false);
		sameDayAdventure.setState(State.RESERVE_ACTIVITY);

		new Expectations() {
			{   
				broker.getBuyer();
				this.result=NIF;
				broker.getIBAN();
				this.result=IBAN;
				ActivityInterface.reserveActivity(begin, begin, AGE,NIF, IBAN);
				this.result = ACTIVITY_CONFIRMATION;
			}
		};

		sameDayAdventure.process();

		Assert.assertEquals(State.PROCESS_PAYMENT, sameDayAdventure.getState());
	}

	@Test
	public void successBookRoom(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{
				broker.getBuyer();
				this.result=NIF;
				broker.getIBAN();
				this.result=IBAN;
				ActivityInterface.reserveActivity(begin, end, AGE,NIF, IBAN);
				this.result = ACTIVITY_CONFIRMATION;
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.BOOK_ROOM, this.adventure.getState());
	}

	@Test
	public void activityException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{   
				broker.getBuyer();
				this.result=NIF;
				broker.getIBAN();
				this.result=IBAN;
				ActivityInterface.reserveActivity(begin, end, AGE ,NIF, IBAN);
				this.result = new ActivityException();
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.UNDO, this.adventure.getState());
	}

	@Test
	public void singleRemoteAccessException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{   
				broker.getBuyer();
				this.result=NIF;
				broker.getIBAN();
				this.result=IBAN;
				ActivityInterface.reserveActivity(begin, end, AGE,NIF, IBAN);
				this.result = new RemoteAccessException();
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.RESERVE_ACTIVITY, this.adventure.getState());
	}

	@Test
	public void maxRemoteAccessException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{   
				broker.getBuyer();
				this.result=NIF;
				broker.getIBAN();
				this.result=IBAN;
				ActivityInterface.reserveActivity(begin, end, AGE,NIF, IBAN);
				this.result = new RemoteAccessException();
			}
		};

		this.adventure.process();
		this.adventure.process();
		this.adventure.process();
		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.UNDO, this.adventure.getState());
	}

	@Test
	public void maxMinusOneRemoteAccessException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{   
				broker.getBuyer();
				this.result=NIF;
				broker.getIBAN();
				this.result=IBAN;
				ActivityInterface.reserveActivity(begin, end, AGE,NIF, IBAN);
				this.result = new RemoteAccessException();
			}
		};

		this.adventure.process();
		this.adventure.process();
		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.RESERVE_ACTIVITY, this.adventure.getState());
	}

	@Test
	public void twoRemoteAccessExceptionOneSuccess(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{   
				broker.getBuyer();
				this.result=NIF;
				broker.getIBAN();
				this.result=IBAN;
				ActivityInterface.reserveActivity(begin, end, AGE,NIF, IBAN);
				this.result = new Delegate() {
					int i = 0;

					public String delegate() {
						if (this.i < 2) {
							this.i++;
							throw new RemoteAccessException();
						} else {
							return ACTIVITY_CONFIRMATION;
						}
					}
				};
				this.times = 3;

			}
		};

		this.adventure.process();
		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.BOOK_ROOM, this.adventure.getState());
	}

	@Test
	public void oneRemoteAccessExceptionOneActivityException(@Mocked final ActivityInterface activityInterface) {
		new Expectations() {
			{   
				broker.getBuyer();
				this.result=NIF;
				broker.getIBAN();
				this.result=IBAN;
				ActivityInterface.reserveActivity(begin, end, AGE,NIF, IBAN);
				this.result = new Delegate() {
					int i = 0;

					public String delegate() {
						if (this.i < 1) {
							this.i++;
							throw new RemoteAccessException();
						} else {
							throw new ActivityException();
						}
					}
				};
				this.times = 2;
			}
		};

		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.UNDO, this.adventure.getState());
	}
}