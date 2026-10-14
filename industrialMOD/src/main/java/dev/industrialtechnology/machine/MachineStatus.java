package dev.industrialtechnology.machine;

public enum MachineStatus {
    IDLE, MINING, STOPPED, NO_POWER, OUTPUT_FULL, COMPLETED, CHUNK_LOADING_ERROR, ACTIVE, DISABLED;
    public String label() { return name().replace('_', ' '); }
}
