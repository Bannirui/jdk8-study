package test.basicwrapper;

/**
 *@author dingrui
 *@date 2021-03-15
 *@description
 */
public enum SeasonEnum {

    SPRING("春天"),
    SUMMER("夏天"),
    AUTUMN("秋天"),
    WINTER("冬天");

    private final String desc;

    private SeasonEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }
}
