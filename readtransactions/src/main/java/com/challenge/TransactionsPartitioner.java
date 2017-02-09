package com.challenge;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TransactionsPartitioner implements Partitioner {
    private static final String DEFAULT_KEY_NAME = "fileName";
    private static final String PARTITION_KEY = "partition";
    private Resource[] resources = new Resource[0];
    private String keyName = DEFAULT_KEY_NAME;

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>();
        int i = 0;
        for (Resource resource : resources) {
            ExecutionContext context = new ExecutionContext();
            Assert.state(resource.exists(), "Resource does not exist: "+resource);
            try {
                context.putString(keyName, resource.getURL().toExternalForm());
            } catch (IOException e) {
                throw new IllegalArgumentException("File could not be located for: "+resource, e);
            }
            map.put(PARTITION_KEY + i, context);
            i++;
        }
        return map;
    }


}
