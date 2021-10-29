package ca.ma99us.jab.headers.groups;

import ca.ma99us.jab.headers.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class MessagePackHeaderGroup<P> extends AbstractHeaderGroup<P> {
    private final ChecksumHeader<P> checksumHeader = new ChecksumHeader<P>();
    @JsonIgnore
    private final MessagePackHeader<P> messagePackHeader = new MessagePackHeader<P>();
    @JsonIgnore
    private final ToStringHeader<P> toStringHeader = new ToStringHeader<P>();

    @Override
    protected JabHeader<P>[] headers() {
        return new JabHeader[]{checksumHeader, messagePackHeader, toStringHeader};
    }
}
