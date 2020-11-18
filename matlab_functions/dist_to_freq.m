function f = dist_to_frequency(x, modo, cont, tonalita)

if cont == 0
    note = 0;
    switch tonalita
        case 1
            note = 261.63;
        case 2
            note = 277.18;
        case 3
            note = 293.66;
        case 4
            note = 311.13;
        case 5
            note = 329.63;
        case 6
            note = 349.23;
        case 7
            note = 369.99;
        case 8
            note = 392;
        case 9
            note = 415.30;
        case 10
            note = 440;
        case 11
            note = 466.16;
        case 12
            note = 493.88;
    end

    if modo == 0

        if x<=10
            f = double(90);
        elseif x>10 && x<60
            f = double(note/2^(1/12));
        elseif x>=60 && x<100
            f = double(note);
        elseif x>=100 && x<140
            f = double(note*2^(2/12));
        elseif x>=140 && x<180
            f = double(note*2^(4/12));
        elseif x>=180 && x<220
            f = double(note*2^(5/12));
        elseif x>=220 && x<260
            f = double(note*2^(7/12));
        elseif x>=260 && x<300
            f = double(note*2^(9/12));
        elseif x>=300 && x<340
            f = double(note*2^(11/12));
        else
            f = double(note*2);

        end
    else
        if x<=10
            f = double(90);
        elseif x>10 && x<60
            f = double(note/2^(2/12));
        elseif x>=60 && x<100
            f = double(note);
        elseif x>=100 && x<140
            f = double(note*2^(2/12));
        elseif x>=140 && x<180
            f = double(note*2^(3/12));
        elseif x>=180 && x<220
            f = double(note*2^(5/12));
        elseif x>=220 && x<260
            f = double(note*2^(7/12));
        elseif x>=260 && x<300
            f = double(note*2^(8/12));
        elseif x>=300 && x<340
            f = double(note*2^(10/12));
        else
            f = double(note*2);

        end
    end
else
    f = double(233.08+x);
end
    
