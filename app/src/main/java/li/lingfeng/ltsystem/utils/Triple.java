package li.lingfeng.ltsystem.utils;

import java.util.Objects;

/**
 * Created by sv on 18-2-14.
 */

public class Triple<F, S, T> {
    public final F first;
    public final S second;
    public final T third;

    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Triple)) {
            return false;
        }
        Triple t = (Triple) obj;
        return Objects.equals(t.first, first) && Objects.equals(t.second, second)
                && Objects.equals(t.third, third);
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode())
                ^ (third == null ? 0 : third.hashCode());
    }

    @Override
    public String toString() {
        return "Triple{" + String.valueOf(first) + "," + String.valueOf(second) + "," + String.valueOf(third) + "}";
    }
}
