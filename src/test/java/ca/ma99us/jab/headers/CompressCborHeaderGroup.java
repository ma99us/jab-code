package ca.ma99us.jab.headers;

import ca.ma99us.jab.headers.groups.AbstractHeaderGroup;
import lombok.Data;

@Data
public class CompressCborHeaderGroup<P> extends AbstractHeaderGroup<P> {
    private final ChecksumHeader<P> checksumHeader = new ChecksumHeader<P>();
    private final CborArrayHeader<P> cborArrayHeader = new CborArrayHeader<P>();
    private final CompressHeader<P> compressHeader = new CompressHeader<P>();
    private final ToStringHeader<P> toStringHeader = new ToStringHeader<P>();

    @Override
    protected JabHeader<P>[] headers() {
        return new JabHeader[]{checksumHeader, cborArrayHeader, compressHeader, toStringHeader};
    }
}
