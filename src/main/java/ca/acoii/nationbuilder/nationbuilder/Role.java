package ca.acoii.nationbuilder.nationbuilder;

public enum Role {
    TREASURER(BonusType.TAX_INCOME), POWER_MINISTER(BonusType.POWER_OUTPUT), INDUSTRY_MINISTER(BonusType.INDUSTRY_OUTPUT), AGRICULTURE_MINISTER(BonusType.AGRICULTURE_OUTPUT), TRADE_MINISTER(BonusType.TRADE_INCOME), LOGISTICS_OVERSEER(BonusType.LOGISTICS_COST);

    private BonusType bonusType;

    Role(BonusType bonusType) {
        this.bonusType = bonusType;
    }
    public BonusType getBonusType() {
        return bonusType;
    }
}
