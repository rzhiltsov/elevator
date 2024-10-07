package org.example.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Call {

    @NotNull
    private int floor;

    @NotNull
    private boolean up;

    @NotNull
    private boolean down;
}
