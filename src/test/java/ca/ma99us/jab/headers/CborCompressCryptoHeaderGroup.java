package ca.ma99us.jab.headers;

import ca.ma99us.jab.headers.groups.AbstractHeaderGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class CborCompressCryptoHeaderGroup<P> extends AbstractHeaderGroup<P> {
    private final ChecksumHeader<P> checksumHeader = new ChecksumHeader<P>();
    @JsonIgnore
    private final CborArrayHeader<P> cborArrayHeader = new CborArrayHeader<P>();
    @JsonIgnore
    private final CompressHeader<P> compressHeader = new CompressHeader<P>();
    private final CryptoHeader<P> cryptoHeader = new CryptoHeader<P>();
    @JsonIgnore
    private final ToStringHeader<P> toStringHeader = new ToStringHeader<P>();

    @Override
    protected JabHeader<P>[] headers() {
        return new JabHeader[]{checksumHeader, cborArrayHeader, compressHeader, cryptoHeader, toStringHeader};
    }
}
