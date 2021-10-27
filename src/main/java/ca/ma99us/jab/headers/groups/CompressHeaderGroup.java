package ca.ma99us.jab.headers.groups;

import ca.ma99us.jab.headers.*;
import lombok.Data;

@Data
public class CompressHeaderGroup<P> extends AbstractHeaderGroup<P> {
    private final ChecksumHeader<P> checksumHeader = new ChecksumHeader<P>();
    private final NoNullsHeader<P> noNullsHeader = new NoNullsHeader<P>();
    private final CompressHeader<P> compressHeader = new CompressHeader<P>();
    private final ToStringHeader<P> toStringHeader = new ToStringHeader<P>();

    @Override
    protected JabHeader<P>[] headers() {
        return new JabHeader[]{checksumHeader, noNullsHeader, compressHeader, toStringHeader};
    }
}
