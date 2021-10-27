package ca.ma99us.jab.headers;

import ca.ma99us.jab.headers.groups.AbstractHeaderGroup;
import lombok.Data;

@Data
public class CompressBsonHeaderGroup<P> extends AbstractHeaderGroup<P> {
    private final ChecksumHeader<P> checksumHeader = new ChecksumHeader<P>();
    private final BsonArrayHeader<P> bsonArrayHeader = new BsonArrayHeader<P>();
    private final CompressHeader<P> compressHeader = new CompressHeader<P>();
    private final ToStringHeader<P> toStringHeader = new ToStringHeader<P>();

    @Override
    protected JabHeader<P>[] headers() {
        return new JabHeader[]{checksumHeader, bsonArrayHeader, compressHeader, toStringHeader};
    }
}
