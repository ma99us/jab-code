package ca.ma99us.jab.headers.groups;

import ca.ma99us.jab.headers.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CryptoHeaderGroup<P> extends AbstractHeaderGroup<P> {
    private final ChecksumHeader<P> checksumHeader = new ChecksumHeader<P>();
    @JsonIgnore
    private final NoNullsHeader<P> noNullsHeader = new NoNullsHeader<P>();
    private final CryptoHeader<P> cryptoHeader = new CryptoHeader<P>();
    @JsonIgnore
    private final ToStringHeader<P> toStringHeader = new ToStringHeader<P>();

    @Override
    protected JabHeader<P>[] headers() {
        return new JabHeader[]{checksumHeader, noNullsHeader, cryptoHeader, toStringHeader};
    }
}
