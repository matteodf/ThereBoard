function y  = concat_bytes(u)

    x1 = toUintVal(u(2));
    x2 = toUintVal(u(3));

    y  = x1 + bitsll(x2, 8);

end

function uintval = toUintVal(val)
%{
dato un intero su 8 bit,
- se positivo lo restituisce come int16
- se negativo lo interpreta come intero positivo.

esempio:
84  = 0101 0100 bin -> 84
-84 = 1010 1100 bin -> 172

%}
    uintval = int32(bitand(val, int8(127)));
    if val < 0
        uintval = bitor(uintval, int32(128));
    end

end
