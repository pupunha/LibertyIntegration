package net.pupunha.liberty.integration.util;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class ComboBoxItem {

    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    private String value;

    public ComboBoxItem(String key) {
        this.key = key;
    }

    public ComboBoxItem(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return getKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComboBoxItem that = (ComboBoxItem) o;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
