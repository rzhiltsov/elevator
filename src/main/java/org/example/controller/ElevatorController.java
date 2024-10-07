package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.Call;
import org.example.elevator.DoorsState;
import org.example.service.ElevatorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ElevatorController {

    private final ElevatorService elevatorService;

    /**
     * Добавляет вызов на этаж с направлением движения.
     */
    @PostMapping("/call")
    public void callElevator(@RequestBody Call call) {
        elevatorService.addCall(call);
    }

    /**
     * Добавляет команду в лифте отправиться на этаж.
     */
    @PostMapping("/select_floor")
    public void selectFloor(@RequestBody int floor) {
        elevatorService.selectFloor(floor);
    }

    /**
     * Указывает команду управления дверями.
     */
    @PostMapping("/set_doors_command")
    public void setDoorsCommand(@RequestBody DoorsState command) {
        elevatorService.setDoorsCommand(command);
    }

}
