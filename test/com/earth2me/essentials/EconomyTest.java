package com.earth2me.essentials;

import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.Economy;
import org.bukkit.World.Environment;
import org.bukkit.plugin.InvalidDescriptionException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class EconomyTest {
	private final transient Essentials ess;
	private static final String NPCNAME = "npc1";
	private static final String PLAYERNAME = "testPlayer1";

	public EconomyTest()
	{
		final FakeServer server = new FakeServer();
		server.createWorld("testWorld", Environment.NORMAL);
		ess = new Essentials(server);
		try
		{
			ess.setupForTesting(server);
		}
		catch (InvalidDescriptionException ex)
		{
			fail("InvalidDescriptionException");
		}
		catch (IOException ex)
		{
			fail("IOException");
		}
		server.addPlayer(new OfflinePlayer(PLAYERNAME, ess.getServer()));
	}

	// only one big test, since we use static instances
	@SuppressWarnings("")
	@Test
	public void testEconomy()
	{
		// test NPC
		assertFalse("NPC does not exists", Economy.playerExists(NPCNAME));
		assertTrue("Create NPC", Economy.createNPC(NPCNAME));
		assertTrue("NPC exists", Economy.playerExists(NPCNAME));
		assertNotNull("NPC can be accessed", ess.getOfflineUser(NPCNAME));
		try
		{
			Economy.removeNPC(NPCNAME);
		}
		catch (UserDoesNotExistException ex)
		{
			fail(ex.getMessage());
		}
		assertFalse("NPC can be removed", Economy.playerExists(NPCNAME));

		//test Math
		try
		{

			assertTrue("Player exists", Economy.playerExists(PLAYERNAME));
			Economy.resetBalance(PLAYERNAME);
			// Player has no money
			assertEquals(0.0, Economy.getMoney(PLAYERNAME), 1e-15);
			Economy.add(PLAYERNAME, 10.0);
			// Add money
			assertEquals(10.0, Economy.getMoney(PLAYERNAME), 1e-15);
			Economy.subtract(PLAYERNAME, 5.0);
			// Subtract money
			assertEquals(5.0, Economy.getMoney(PLAYERNAME), 1e-15);
			Economy.multiply(PLAYERNAME, 2.0);
			// Multiply money
			assertEquals(10.0, Economy.getMoney(PLAYERNAME), 1e-15);
			Economy.divide(PLAYERNAME, 2.0);
			// Divide money
			assertEquals(5.0, Economy.getMoney(PLAYERNAME), 1e-15);
			Economy.setMoney(PLAYERNAME, 10.0);
			// Set money
			assertEquals(10.0, Economy.getMoney(PLAYERNAME), 1e-15);
		}
		catch (NoLoanPermittedException | UserDoesNotExistException ex)
		{
			fail(ex.getMessage());
		}

		//test Format
		assertEquals("Format $1000", "$1000", Economy.format(1000.0));
		assertEquals("Format $10", "$10", Economy.format(10.0));
		assertEquals("Format $10.10", "$10.10", Economy.format(10.10));
		assertEquals("Format $10.10", "$10.10", Economy.format(10.1000001));
		assertEquals("Format $10.10", "$10.10", Economy.format(10.1099999));


		//test Exceptions
		try
		{
			assertTrue("Player exists", Economy.playerExists(PLAYERNAME));
			Economy.resetBalance(PLAYERNAME);
			// "Reset balance",
			assertEquals(0.0, Economy.getMoney(PLAYERNAME), 1e-15);
			Economy.subtract(PLAYERNAME, 5.0);
			fail("Did not throw exception");
		}
		catch (NoLoanPermittedException ignore)
		{
		}
		catch (UserDoesNotExistException ex)
		{
			fail(ex.getMessage());
		}

		try
		{
			Economy.resetBalance("UnknownPlayer");
			fail("Did not throw exception");
		}
		catch (NoLoanPermittedException ex)
		{
			fail(ex.getMessage());
		}
		catch (UserDoesNotExistException ignore)
		{
		}
	}
}
