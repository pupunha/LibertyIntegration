package net.pupunha.liberty.integration.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@AllArgsConstructor
public class ListItem {

    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    private Path value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListItem listItem = (ListItem) o;

        return key != null ? key.equals(listItem.key) : listItem.key == null;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public String toString() {
        return this.key;
    }
}
