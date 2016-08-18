/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.tests.server.component.grid;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.LegacyGrid;
import com.vaadin.ui.LegacyGrid.DetailsGenerator;
import com.vaadin.ui.LegacyGrid.RowReference;

public class GridContainerTest {

    /**
     * Null Stream used with serialization tests
     */
    protected static OutputStream NULLSTREAM = new OutputStream() {
        @Override
        public void write(int b) {
        }
    };

    @Test
    public void testDetailsGeneratorDoesNotResetOnContainerChange() {
        LegacyGrid grid = new LegacyGrid();
        DetailsGenerator detGen = new DetailsGenerator() {

            @Override
            public Component getDetails(RowReference rowReference) {
                return new Label("Empty details");
            }
        };
        grid.setDetailsGenerator(detGen);

        grid.setContainerDataSource(createContainer());

        Assert.assertEquals("DetailsGenerator changed", detGen,
                grid.getDetailsGenerator());
    }

    @Test
    public void testSetContainerTwice() throws Exception {

        TestGrid grid = new TestGrid();

        grid.setContainerDataSource(createContainer());

        // Simulate initial response to ensure "lazy" state changes are done
        // before resetting the datasource
        grid.beforeClientResponse(true);
        grid.getDataProvider().beforeClientResponse(true);

        grid.setContainerDataSource(createContainer());
    }

    @SuppressWarnings("unchecked")
    private IndexedContainer createContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty("x", String.class, null);
        container.addItem(0).getItemProperty("x").setValue("y");
        return container;
    }

    @Test
    public void setColumnsOrder() {
        LegacyGrid grid = new LegacyGrid();
        IndexedContainer ic = new IndexedContainer();
        ic.addContainerProperty("foo", String.class, "");
        ic.addContainerProperty("baz", String.class, "");
        ic.addContainerProperty("bar", String.class, "");
        grid.setContainerDataSource(ic);
        grid.setColumns("foo", "baz", "bar");

        Assert.assertEquals("foo", grid.getColumns().get(0).getPropertyId());
        Assert.assertEquals("baz", grid.getColumns().get(1).getPropertyId());
        Assert.assertEquals("bar", grid.getColumns().get(2).getPropertyId());
    }

    @Test
    public void addColumnNotInContainer() {
        LegacyGrid grid = new LegacyGrid();
        grid.setContainerDataSource(new IndexedContainer());
        try {
            grid.addColumn("notInContainer");
            Assert.fail(
                    "Adding a property id not in the container should throw an exception");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("notInContainer"));
            Assert.assertTrue(
                    e.getMessage().contains("does not exist in the container"));
        }
    }

    @Test
    public void setColumnsForPropertyIdNotInContainer() {
        LegacyGrid grid = new LegacyGrid();
        grid.setContainerDataSource(new IndexedContainer());
        try {
            grid.setColumns("notInContainer", "notThereEither");
            Assert.fail(
                    "Setting columns for property ids not in the container should throw an exception");
        } catch (IllegalStateException e) {
            // addColumn is run in random order..
            Assert.assertTrue(e.getMessage().contains("notInContainer")
                    || e.getMessage().contains("notThereEither"));
            Assert.assertTrue(
                    e.getMessage().contains("does not exist in the container"));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void multipleAddColumnsForDefaultContainer() {
        LegacyGrid grid = new LegacyGrid();
        grid.addColumn("foo");
        grid.addColumn("foo");
    }

    @Test
    public void testSerializeRpcDataProviderWithRowChanges()
            throws IOException {
        LegacyGrid grid = new LegacyGrid();
        IndexedContainer container = new IndexedContainer();
        grid.setContainerDataSource(container);
        container.addItem();
        serializeComponent(grid);
    }

    protected void serializeComponent(Component component) throws IOException {
        ObjectOutputStream stream = null;
        try {
            stream = new ObjectOutputStream(NULLSTREAM);
            stream.writeObject(component);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
