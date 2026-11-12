public class App {
    public static void main(String[] args) throws Exception {
        
    }

    public enum DayOfWeek{
        MONDAY, TUESDAY,WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
        public boolean isWeekend(){
        return this == SATURDAY || this == SUNDAY;
    }
    }


    public enum AccessLevel{
        GUEST, USER, MODER, ADMIN;
        public boolean canEditContent(){
            return this == MODER || this == ADMIN;
        }
    }

    public enum Packagigng{
        BOTTLE, BOX, BAG;
        public String getDisposalInfo(){
            switch (this) {
                case BOTTLE: 
                    return "Сдать в контейнер пластика";
                case BOX:
                    return "Сложить и сдать в макулатуру";
                case BAG:
                    return "Сдать в пункт приемки";
                default:
                    return "Неизвестынй тип";
            }
        }
    }

    public enum ClothingSize {
    S(160, 170, 50, 65),
    M(170, 180, 65, 80),
    L(180, 190, 80, 95),
    XL(190, 205, 95, 115);

    private final int minHeight;
    private final int maxHeight;
    private final int minWeight;
    private final int maxWeight;

    ClothingSize(int minHeight, int maxHeight, int minWeight, int maxWeight) {
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
    }

    public boolean fits(int height, int weight) {
        return height >= minHeight && height <= maxHeight &&
               weight >= minWeight && weight <= maxWeight;
    }
    }

    public enum TrafficLight {
    RED, YELLOW, GREEN;

    public String getAction() {
        switch (this) {
            case RED:
                return "Остановиться";
            case YELLOW:
                return "Осторожно";
            case GREEN:
                return "Двигаться";
            default:
                return "";
        }
    }
    }
    
}
