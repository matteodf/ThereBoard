function [x, t, f_old] = sinewave_generator(A, F0, gyro, inst, t, f_old)
%{
  Funzione che genera una onda sinusoidale di
- frequenza F0 [Hz]
- ampiezza A [compresa tra 0 e 32737]

Il risultato è un array di N valori generati partendo dal tempo t calcolato
rispetto alla computazione precedente.
Al termine della computazione il tempo è ridotto di n periodi per limitarne
la crescita.
%}

Fs=8000; %[Hz]
Ts=1/Fs; %[s]
N=8;%[S]

x = int16(zeros(N,1));

if F0 > 90

    t = f_old * t / F0;
    f_old = F0;
    F1 = 2*F0;
    F2 = 3*F0;
    F3 = 4*F0;
    A1=A/2;
    A2=A/3;
    A3=A/4;

    if gyro(2) > 1
        volinc = double(gyro(2)/10);
    elseif gyro(3) <-1
        volinc = double(-gyro(2)/10);
    else
        volinc = 1;
    end

    for n=0:N-1
        if inst == 0
            x(n+1)=int16(volinc*(A*sin(2*pi*F0*t*Ts) + A1*sin(2*pi*F1*t*Ts) + A2*sin(2*pi*F2*t*Ts) + A3*sin(2*pi*F3*t*Ts))/4); %modello matematico A(t)= A Sin(2?ft) tono puro discretizzato
        else
            x(n+1)=int16(volinc*A*sin(2*pi*F0*t*Ts));
        end
        t = t + 1;
    end

    T = Fs/F0;
    t = t - T*floor(t/T);

else
    t = f_old * t / F0;
    f_old = F0;


    for n=0:N-1
        x(n+1)=0;
        t = t + 1;
    end

    T = Fs/F0;
    t = t - T*floor(t/T);
end
