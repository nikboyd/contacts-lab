package dev.educery.data;

import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import dev.educery.utils.Logging;

/**
 * Confirms proper operation of a data source.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(value = { "direct" }) // pick: direct or cloud
@ContextConfiguration(classes = { CloudDataSource.class, DirectDataSource.class })
public class DataSourceTest implements Logging {

    @Autowired(required = false)
    CloudDataSource cloudDataSource;

    @Autowired(required = false)
    DirectDataSource directDataSource;

    @Test public void loadedSource() {
        if (directDataSource != null) report("loaded DirectDataSource");
        if (cloudDataSource != null) report("loaded CloudDataSource");
    }

} // DataSourceTest
