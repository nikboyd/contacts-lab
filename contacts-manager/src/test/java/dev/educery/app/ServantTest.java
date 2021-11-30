package dev.educery.app;

import org.junit.*;
import dev.educery.domain.ItemBrief;
import dev.educery.services.ClientProxy;
import dev.educery.utils.Logging;

/**
 * Confirms proper service startup and shutdown.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Ignore
public class ServantTest implements Logging {

    ClientProxy contacts = new ClientProxy();

    @Test public void launch() {
        Servant.startServer();
        report("started service");
        Servant.waitForServer();

        ItemBrief b = contacts.countContacts();
        report(b.toString());
        Servant.stopServer();
        report("stopped service");
    }

} // ServantTest
