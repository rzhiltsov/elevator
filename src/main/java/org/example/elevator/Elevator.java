package org.example.elevator;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.Call;

import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Elevator {

    @Getter
    private final LinkedHashMap<Integer, Call> movements = new LinkedHashMap<>();

    @Getter
    private final ReentrantLock movementLock = new ReentrantLock();

    @Getter
    private volatile int currentFloor = 1;

    @Setter
    private volatile DoorsState doorsCommand = null;

    private DoorsState doorsStatus = DoorsState.CLOSE;

    /**
     * Проверяет текущие задачи, сначала проверяет команды управления дверями, затем берёт первую задачу
     * из маршрутного листа и отправляет лифт в ту стороны до самого дальнего вызова, который он сейчас получил.
     */
    public void checkWork() {
        checkDoors();
        movementLock.lock();
        while (!movements.isEmpty()) {
            int floor = movements.firstEntry().getKey();
            if (currentFloor < floor) {
                int maxFloor = movements.keySet().stream().max(Integer::compareTo).get();
                movementLock.unlock();
                while (currentFloor < maxFloor) {
                    moveUp(maxFloor);
                }
            } else if (currentFloor > floor) {
                int minFloor = movements.keySet().stream().min(Integer::compareTo).get();
                movementLock.unlock();
                while (currentFloor > minFloor) {
                    moveDown(minFloor);
                }
            }
        }
        if (movementLock.getHoldCount() > 0) {
            movementLock.unlock();
        }
    }

    /**
     * Перемещает лифт вверх на один этаж и останавливает его при необходимости.
     */
    private void moveUp(int targetFloor) {
        movementLock.lock();
        log.info("Лифт движется вверх, текущий этаж: {}", currentFloor++);
        Call call = movements.get(currentFloor);
        if ((call != null && call.isUp()) || currentFloor == targetFloor) {
            movements.remove(currentFloor);
            movementLock.unlock();
            stop();
        } else {
            movementLock.unlock();
        }
    }

    /**
     * Перемещает лифт вниз на один этаж и останавливает его при необходимости.
     */
    private void moveDown(int targetFloor) {
        movementLock.lock();
        log.info("Лифт движется вниз, текущий этаж: {}", currentFloor--);
        Call call = movements.get(currentFloor);
        if ((call != null && call.isDown()) || currentFloor == targetFloor) {
            movements.remove(currentFloor);
            movementLock.unlock();
            stop();
        } else {
            movementLock.unlock();
        }
    }

    /**
     * Останавливает лифт, открывает двери и проверяет команды управления дверями.
     */
    private void stop() {
        log.info("Лифт остановился, текущий этаж: {}", currentFloor);
        openDoors();
        checkDoors();
    }

    /**
     * Проверяет команды управления дверями и запускает действие.
     */
    private void checkDoors() {
        while (doorsCommand != null) {
            switch (doorsCommand) {
                case OPEN -> {
                    if (doorsStatus == DoorsState.CLOSE) {
                        openDoors();
                    } else {
                        doorsCommand = null;
                    }
                }
                case CLOSE -> {
                    if (doorsStatus == DoorsState.OPEN) {
                        closeDoors();
                    } else {
                        doorsCommand = null;
                    }
                }
            }
        }
    }

    /**
     * Открывает двери лифта и делает паузу.
     */
    @SneakyThrows(InterruptedException.class)
    private void openDoors() {
        Thread.sleep(1_000);
        doorsStatus = DoorsState.OPEN;
        log.info("Двери открылись");
        doorsCommand = DoorsState.CLOSE;
        Thread.sleep(5_000);
    }

    /**
     * Закрывает двери лифт и делает паузу.
     */
    @SneakyThrows(InterruptedException.class)
    private void closeDoors() {
        Thread.sleep(1_000);
        doorsStatus = DoorsState.CLOSE;
        log.info("Двери закрылись");
        doorsCommand = null;
        Thread.sleep(2_000);
    }
}
