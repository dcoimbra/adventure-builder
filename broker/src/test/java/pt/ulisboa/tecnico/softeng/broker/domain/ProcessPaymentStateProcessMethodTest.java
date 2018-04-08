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
import pt.ulisboa.tecnico.softeng.bank.exception.BankException;
import pt.ulisboa.tecnico.softeng.broker.domain.Adventure.State;
import pt.ulisboa.tecnico.softeng.broker.exception.RemoteAccessException;
import pt.ulisboa.tecnico.softeng.broker.interfaces.BankInterface;
import pt.ulisboa.tecnico.softeng.broker.interfaces.TaxInterface;
import pt.ulisboa.tecnico.softeng.tax.dataobjects.InvoiceData;

@RunWith(JMockit.class)
public class ProcessPaymentStateProcessMethodTest {
	private static final String IBAN = "BK01987654321";
	private static final String NIF = "123456789";
	private static final String SELLER = "145656766";
	private static final int AGE = 20;
	private static final int AMOUNT = 300;
	private static final double MARGIN = 0.3;
	private static final String PAYMENT_CONFIRMATION = "PaymentConfirmation";
	private static final String TAX_CONFIRMATION = "TaxConfirmation";
	private final LocalDate begin = new LocalDate(2016, 12, 19);
	private final LocalDate end = new LocalDate(2016, 12, 21);
	private Adventure adventure;
	private static final String DRIVING_LICENSE = "IMT1234";



	@Injectable
	private Broker broker;

	private Client client;

	@Before
	public void setUp() {
		this.client = new Client(this.broker, IBAN, NIF, DRIVING_LICENSE ,AGE);
		this.adventure = new Adventure(this.broker, this.begin, this.end, this.client, MARGIN, true);
		this.adventure.setState(State.PROCESS_PAYMENT);
	}

	@Test
	public void success(@Mocked final BankInterface bankInterface,@Mocked final TaxInterface taxInterface) {
		new Expectations() {
			{
				TaxInterface.submitInvoice((InvoiceData) this.any);
				this.result = TAX_CONFIRMATION;
				
				BankInterface.processPayment(IBAN, 0);
				this.result = PAYMENT_CONFIRMATION;
				
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.CONFIRMED, this.adventure.getState());
	}
	

	@Test
	public void bankException(@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(IBAN, 0);
				this.result = new BankException();
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.UNDO, this.adventure.getState());
	}

	@Test
	public void singleRemoteAccessException(@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(IBAN, 0);
				this.result = new RemoteAccessException();
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.PROCESS_PAYMENT, this.adventure.getState());
	}

	@Test
	public void oneRemoteAccessException(@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{
				BankInterface.processPayment(IBAN, 0);
				this.result = new RemoteAccessException();
			}
		};

		this.adventure.process();

		Assert.assertEquals(State.PROCESS_PAYMENT, this.adventure.getState());
	}

	@Test
	public void maxMinusOneRemoteAccessException(@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{

				BankInterface.processPayment(IBAN, 0);
				this.result = new RemoteAccessException();
			}
		};

		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.PROCESS_PAYMENT, this.adventure.getState());
	}

	@Test
	public void twoRemoteAccessExceptionOneSuccess(@Mocked final BankInterface bankInterface, @Mocked final TaxInterface taxInterface) {
		new Expectations() {
			{
				TaxInterface.submitInvoice((InvoiceData) this.any);
				this.result = TAX_CONFIRMATION;

				BankInterface.processPayment(IBAN, 0);
				this.result = new Delegate() {
					int i = 0;

					public String delegate() {
						if (this.i < 2) {
							this.i++;
							throw new RemoteAccessException();
						} else {
							return PAYMENT_CONFIRMATION;
						}
					}
				};
				this.times = 3;

			}
		};

		this.adventure.process();
		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.CONFIRMED, this.adventure.getState());
	}

	@Test
	public void oneRemoteAccessExceptionOneBankException(@Mocked final BankInterface bankInterface) {
		new Expectations() {
			{

				BankInterface.processPayment(IBAN, 0);
				this.result = new Delegate() {
					int i = 0;

					public void delegate() {
						if (this.i < 3) {
							this.i++;
							throw new RemoteAccessException();
						}

					}
				};
				this.times = 3;

			}
		};

		this.adventure.process();
		this.adventure.process();
		this.adventure.process();
		this.adventure.process();

		Assert.assertEquals(State.CANCELLED, this.adventure.getState());
	}


}