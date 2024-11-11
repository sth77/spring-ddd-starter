package com.example.app.common;

import com.example.app.common.model.Command;

public interface CommandPublisher {
    
    void publishCommand(Command command);

}
