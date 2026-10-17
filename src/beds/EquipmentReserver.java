package beds;

public class EquipmentReserver {
    public void check(boolean equipmentAvailable) {
        if (!equipmentAvailable) {
            throw new IllegalStateException("Required equipment is unavailable.");
        }
    }
}
