package ca.ma99us.jab.headers.groups;

import ca.ma99us.jab.headers.*;
import lombok.Data;

@Data
public class CborHeaderGroup<P> extends AbstractHeaderGroup<P> {
    private final ChecksumHeader<P> checksumHeader = new ChecksumHeader<P>();
    private final CborArrayHeader<P> cborArrayHeader = new CborArrayHeader<P>();
    private final ToStringHeader<P> toStringHeader = new ToStringHeader<P>();

    @Override
    protected JabHeader<P>[] headers() {
        return new JabHeader[]{checksumHeader, cborArrayHeader, toStringHeader};
    }
}