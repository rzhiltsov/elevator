package org.example.service;

import org.example.dto.Call;
import org.example.elevator.DoorsState;
import org.example.elevator.Elevator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ElevatorService {

    private final Elevator elevator = new Elevator();

    @Value("${elevator.min-floor}")
    private int minFloor;

    @Value("${elevator.max-floor}")
    private int maxFloor;

    /**
     * Добавляет в маршрутный лист вызов на этаж с направлением движения, позволяет указать все направления сразу.
     */
    public void addCall(Call call) {
        elevator.getMovementLock().lock();
        if (validateFloor(call.getFloor())) {
            Call presentCall = elevator.getMovements().get(call.getFloor());
            if (presentCall != null) {
                if (call.isUp()) {
                    presentCall.setUp(true);
                }
                if (call.isDown()) {
                    presentCall.setDown(true);
                }
            } else if (call.isDown() || call.isUp()) {
                elevator.getMovements().put(call.getFloor(), call);
            }
        }
        elevator.getMovementLock().unlock();
    }

    /**
     * Добавляет в маршрутный лист этаж, выбранный в лифте, если лифт будет проезжать через этот этаж,
     * то всегда остановится.
     */
    public void selectFloor(int floor) {
        elevator.getMovementLock().lock();
        if (validateFloor(floor)) {
            Call call = new Call();
            call.setFloor(floor);
            call.setUp(true);
            call.setDown(true);
            elevator.getMovements().put(floor, call);
        }
        elevator.getMovementLock().unlock();
    }

    /**
     * Указывает команду управления дверями, она сработает, если лифт остановился.
     */
    public void setDoorsCommand(DoorsState command) {
        elevator.setDoorsCommand(command);
    }

    /**
     * Заставляет лифт проверять задачи, когда он бездействует.
     */
    @Scheduled(fixedRate = 1_000)
    private void checkWork() {
        elevator.checkWork();
    }

    /**
     * Проверяет допустимость значения этажа.
     */
    private boolean validateFloor(int floor) {
        int unsupportedFloor = 0;
        return floor != elevator.getCurrentFloor() && floor != unsupportedFloor &&
                floor >= minFloor && floor <= maxFloor;
    }
}
