/* ==========================================================================
 * GNU GENERAL PUBLIC LICENSE
 * Version 2, June 1991
 * 
 * Copyright (C) 1989, 1991 Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * 
 * $LastChangeDate$
 * $Rev$
 * $LastChangedBy$
 * $URL$
 * $Id$
 * ========================================================================== */ 
package com.arykow.applications.ugabe.client;


public final class AudioController {
	public boolean isMuted = false;
	public int IO[];
	public int WAVE[] = new int[16];
	private int sampleRate = 44100;
	public int cyclesLeftToRender;
	public int TimerCountDown;
	public boolean SweepTimerTick;
	private int RATE;
	public int CRATE;
	public double currentEmulationSpeed = 1.0;

	boolean[] channelactive = new boolean[5];

	public AudioControllerSoundRegister S1;
	public AudioControllerSoundRegister S2;
	public AudioControllerSoundRegister S3;
	public AudioControllerSoundRegister S4;

	protected CPU cpu;

	public boolean channelActive(int i) {
		if (!(i >= 1))
			throw new Error("Assertion failed: " + "i>=1");
		if (!(i <= 4))
			throw new Error("Assertion failed: " + "i<=4");
		return channelactive[i];
	}

	public void setChannelActive(int i, boolean b) {
		if (!(i >= 1))
			throw new Error("Assertion failed: " + "i>=1");
		if (!(i <= 4))
			throw new Error("Assertion failed: " + "i<=4");
		channelactive[i] = b;
	}

	public boolean toggleChannelOnOff(int i) {
		channelactive[i] ^= true;
		return channelactive[i];
	}

	IAudioListener audioListener = null;

	public void addIAudioListener(IAudioListener audioListener) {
		this.audioListener = audioListener;
	}

	public AudioController(CPU cpu) {
		this.cpu = cpu;
		this.audioListener = null;
		IO = new int[0x30];
		cyclesLeftToRender = 0;
		S1 = new AudioControllerSoundRegister();
		S2 = new AudioControllerSoundRegister();
		S3 = new AudioControllerSoundRegister();
		S4 = new AudioControllerSoundRegister();
		TimerCountDown = 16384;
		SweepTimerTick = false;
		RATE = (1 << 21) / sampleRate;
		setSpeed(1.0);
	}

	public void setSampleRate(int r) {
		sampleRate = r;
		RATE = (1 << 21) / sampleRate;
		setSpeed(currentEmulationSpeed);
	}

	public void setSpeed(double emulationSpeed) {
		currentEmulationSpeed = emulationSpeed;
		CRATE = (int) ((4194304.0 / (double) sampleRate) * emulationSpeed);
	}

	void s1_init() {
		S1.swcnt = 0;
		S1.swfreq = ((IO[0x04] & 7) << 8) + IO[0x03];
		S1.envol = IO[0x02] >> 4;
		S1.endir = (IO[0x02] >> 3) & 1;
		S1.endir |= S1.endir - 1;
		S1.enlen = (IO[0x02] & 7) << 15;
		if (S1.on == 0)
			S1.pos = 0;
		S1.on = 1;
		S1.cnt = 0;
		S1.encnt = 0;
	}

	void s2_init() {
		S2.envol = IO[0x07] >> 4;
		S2.endir = (IO[0x07] >> 3) & 1;
		S2.endir |= S2.endir - 1;
		S2.enlen = (IO[0x07] & 7) << 15;
		if (S2.on == 0)
			S2.pos = 0;
		S2.on = 1;
		S2.cnt = 0;
		S2.encnt = 0;
	}

	void s3_init() {
		int i;
		if (S3.on == 0)
			S3.pos = 0;
		S3.cnt = 0;
		S3.on = IO[0x0a] >> 7;
		if (S3.on != 0)
			for (i = 0; i < 0xf; i++)
				IO[i + 0x20] = 0x13 ^ IO[i + 0x21];
		IO[0x2f] = 0x13 ^ cpu.videoController.LCDC;
	}

	void s4_init() {
		S4.envol = IO[0x11] >> 4;
		S4.endir = (IO[0x11] >> 3) & 1;
		S4.endir |= S4.endir - 1;
		S4.enlen = (IO[0x11] & 7) << 15;
		S4.on = 1;
		S4.pos = 0;
		S4.cnt = 0;
		S4.encnt = 0;
	}

	private void s1_freq_d(int d) {
		if (RATE > (d << 4))
			S1.freq = 0;
		else
			S1.freq = (RATE << 17) / d;
	}

	private void s1_freq() {
		s1_freq_d(2048 - (((IO[0x04] & 7) << 8) + IO[0x03]));
	}

	private void s2_freq() {
		int d = 2048 - (((IO[0x09] & 7) << 8) + IO[0x08]);
		if (RATE > (d << 4))
			S2.freq = 0;
		else
			S2.freq = (RATE << 17) / d;
	}

	private void s3_freq() {
		int d = 2048 - (((IO[0x0e] & 7) << 8) + IO[0x0d]);
		if (RATE > (d << 3))
			S3.freq = 0;
		else
			S3.freq = (RATE << 21) / d;
	}

	private void s4_freq() {
		S4.freq = (freqtab[IO[0x12] & 7] >> (IO[0x12] >> 4)) * RATE;
		if ((S4.freq >> 18) != 0)
			S4.freq = 1 << 18;
	}

	private void sound_dirty() {
		S1.swlen = ((IO[0x00] >> 4) & 7) << 14;
		S1.len = (64 - (IO[0x01] & 63)) << 13;
		S1.envol = IO[0x02] >> 4;
		S1.endir = (IO[0x02] >> 3) & 1;
		S1.endir |= S1.endir - 1;
		S1.enlen = (IO[0x02] & 7) << 15;
		s1_freq();
		S2.len = (64 - (IO[0x06] & 63)) << 13;
		S2.envol = IO[0x07] >> 4;
		S2.endir = (IO[0x07] >> 3) & 1;
		S2.endir |= S2.endir - 1;
		S2.enlen = (IO[0x07] & 7) << 15;
		s2_freq();
		S3.len = (256 - IO[0x0b]) << 20;
		s3_freq();
		S4.len = (64 - (IO[0x10] & 63)) << 13;
		S4.envol = IO[0x11] >> 4;
		S4.endir = (IO[0x11] >> 3) & 1;
		S4.endir |= S4.endir - 1;
		S4.enlen = (IO[0x11] & 7) << 15;
		s4_freq();
	}

	protected void sound_off() {
		IO[0x00] = 0x80;
		IO[0x01] = 0xBF;
		IO[0x02] = 0xF3;
		IO[0x04] = 0xBF;
		IO[0x06] = 0x3F;
		IO[0x07] = 0x00;
		IO[0x09] = 0xBF;
		IO[0x0a] = 0x7F;
		IO[0x0b] = 0xFF;
		IO[0x0c] = 0x9F;
		IO[0x0d] = 0xBF;
		IO[0x10] = 0xFF;
		IO[0x11] = 0x00;
		IO[0x12] = 0x00;
		IO[0x13] = 0xBF;
		IO[0x14] = 0x77;
		IO[0x15] = 0xF3;
		IO[0x16] = 0xF1;
		S1.on = 0;
		S2.on = 0;
		S3.on = 0;
		S4.on = 0;
		sound_dirty();
	}

	public void reset() {
		cyclesLeftToRender = 0;
		S1 = new AudioControllerSoundRegister();
		S2 = new AudioControllerSoundRegister();
		S3 = new AudioControllerSoundRegister();
		S4 = new AudioControllerSoundRegister();
		TimerCountDown = 16384;
		SweepTimerTick = false;

		int[] w = cpu.isCGB() ? cgbwave : dmgwave;
		for (int i = 0; i < 0x10; ++i)
			WAVE[i] = w[i];
		for (int i = 0; i < 0x10; ++i)
			IO[i] = WAVE[i];
		for (int i = 0x10; i < 0x30; ++i)
			IO[i] = 0;
		sound_off();

		if (audioListener != null)
			audioListener.reset();
	}

	void sound_mix() {
		int s, l, r, f, n;
		if (RATE == 0)
			return;

		for (; cyclesLeftToRender >= 0; cyclesLeftToRender -= CRATE)

		{
			l = r = 0;

			if (channelactive[1] && S1.on != 0) {
				s = sqwave[IO[0x01] >> 6][(S1.pos >> 18) & 7] & S1.envol;
				S1.pos += S1.freq;
				if (((IO[0x04] & 64) != 0) && ((S1.cnt += RATE) >= S1.len))
					S1.on = 0;
				if ((S1.enlen != 0) && (S1.encnt += RATE) >= S1.enlen) {
					S1.encnt -= S1.enlen;
					S1.envol += S1.endir;
					if (S1.envol < 0)
						S1.envol = 0;
					if (S1.envol > 15)
						S1.envol = 15;
				}
				if ((S1.swlen != 0) && (S1.swcnt += RATE) >= S1.swlen) {
					S1.swcnt -= S1.swlen;
					f = S1.swfreq;
					n = (IO[0x00] & 7);
					if ((IO[0x00] & 8) != 0)
						f -= (f >> n);
					else
						f += (f >> n);
					if (f > 2047)
						S1.on = 0;
					else {
						S1.swfreq = f;
						IO[0x03] = f;
						IO[0x04] = (IO[0x04] & 0xF8) | (f >> 8);
						s1_freq_d(2048 - f);
					}
				}
				s <<= 2;
				if ((IO[0x15] & 1) != 0)
					r += s;
				if ((IO[0x15] & 16) != 0)
					l += s;

			}

			if (channelactive[2] && S2.on != 0) {
				s = sqwave[IO[0x06] >> 6][(S2.pos >> 18) & 7] & S2.envol;
				S2.pos += S2.freq;
				if (((IO[0x09] & 64) != 0) && ((S2.cnt += RATE) >= S2.len))
					S2.on = 0;
				if ((S2.enlen != 0) && (S2.encnt += RATE) >= S2.enlen) {
					S2.encnt -= S2.enlen;
					S2.envol += S2.endir;
					if (S2.envol < 0)
						S2.envol = 0;
					if (S2.envol > 15)
						S2.envol = 15;
				}
				s <<= 2;
				if ((IO[0x15] & 2) != 0)
					r += s;
				if ((IO[0x15] & 32) != 0)
					l += s;

			}

			if (channelactive[3] && S3.on != 0) {
				s = WAVE[(S3.pos >> 22) & 15];
				if ((S3.pos & (1 << 21)) != 0)
					s &= 15;
				else
					s >>= 4;
				s -= 8;
				S3.pos += S3.freq;
				if (((IO[0x0e] & 64) != 0) && ((S3.cnt += RATE) >= S3.len))
					S3.on = 0;
				if ((IO[0x0c] & 96) != 0)
					s <<= (3 - ((IO[0x0c] >> 5) & 3));
				else
					s = 0;
				if ((IO[0x15] & 4) != 0)
					r += s;
				if ((IO[0x15] & 64) != 0)
					l += s;

			}

			if (channelactive[4] && S4.on != 0) {
				if ((IO[0x12] & 8) != 0)
					s = 1 & (noise7[(S4.pos >> 20) & 15] >> (7 - ((S4.pos >> 17) & 7)));
				else
					s = 1 & (noise15[(S4.pos >> 20) & 4095] >> (7 - ((S4.pos >> 17) & 7)));
				s = (-s) & S4.envol;
				S4.pos += S4.freq;
				if (((IO[0x13] & 64) != 0) && ((S4.cnt += RATE) >= S4.len))
					S4.on = 0;
				if ((S4.enlen != 0) && (S4.encnt += RATE) >= S4.enlen) {
					S4.encnt -= S4.enlen;
					S4.envol += S4.endir;
					if (S4.envol < 0)
						S4.envol = 0;
					if (S4.envol > 15)
						S4.envol = 15;
				}
				s += s << 1;
				if ((IO[0x15] & 8) != 0)
					r += s;
				if ((IO[0x15] & 128) != 0)
					l += s;

			}

			l *= (IO[0x14] & 0x07);
			r *= ((IO[0x14] & 0x70) >> 4);
			l >>= 3;
			r >>= 3;

			if (l > 127)
				l = 127;
			else if (l < -128)
				l = -128;
			if (r > 127)
				r = 127;
			else if (r < -128)
				r = -128;

			if (isMuted) {
				l = 0;
				r = 0;
			}

			if (audioListener != null && !speedHax) {

				audioListener.writeAudioSample((byte) l, (byte) r);
			}

		}
		IO[0x16] = (IO[0x16] & 0xf0) | S1.on | (S2.on << 1) | (S3.on << 2) | (S4.on << 3);
	}

	public boolean speedHax = false;

	public void render(int nrCycles) {
		cyclesLeftToRender += nrCycles;
		sound_mix();
	}

	public int read(int index) {
		int i = (index & 0xff) - 0x10;
		if ((i < 0) || (i > 0x3f)) {
			CPULogger.log("AudioController: Error: reading from non sound-address:" + index);
			return -1;
		} else if ((i != 0x16) && ((IO[0x16] & 0x80) == 0)) {
			CPULogger.log("AudioController: Sound disabled: Reads are undefined!");
			return 0;
		} else if ((i == 0x05) || (i == 0x0f) || ((i > 0x16) && (i < 0x20))) {

		}
		sound_mix();
		return IO[i];
	}

	public void write(int index, int value) {
		int i = (index & 0xff) - 0x10;
		if ((i < 0) || (i > 0x2f)) {
			CPULogger.log("AudioController: Error: writing to non sound-address:" + index);
			return;
		} else if ((i != 0x16) && ((IO[0x16] & 0x80) == 0)) {
			CPULogger.log("AudioController: Sound disabled: Writes are undefined!");
			return;
		} else if ((i == 0x05) || (i == 0x0f) || ((i > 0x16) && (i < 0x20))) {

			IO[i] = value;
			return;
		}
		if ((i & 0xF0) == 0x20) {
			if (S3.on != 0)
				sound_mix();
			if (S3.on == 0) {
				WAVE[i & 0x0f] = IO[i] = value;
			}
			return;
		}
		sound_mix();
		IO[i] = value;
		switch (i) {
		case 0x00:

			S1.swlen = ((value >> 4) & 7) << 14;
			S1.swfreq = ((IO[0x04] & 7) << 8) + IO[0x03];
			break;
		case 0x01:

			S1.len = (64 - (value & 63)) << 13;
			break;
		case 0x02:

			S1.envol = value >> 4;
			S1.endir = (value >> 3) & 1;
			S1.endir |= S1.endir - 1;
			S1.enlen = (value & 7) << 15;
			break;
		case 0x03:

			s1_freq();
			break;
		case 0x04:

			s1_freq();
			if ((value & 128) != 0)
				s1_init();
			break;
		case 0x06:

			S2.len = (64 - (value & 63)) << 13;
			break;
		case 0x07:

			S2.envol = value >> 4;
			S2.endir = (value >> 3) & 1;
			S2.endir |= S2.endir - 1;
			S2.enlen = (value & 7) << 15;
			break;
		case 0x08:

			s2_freq();
			break;
		case 0x09:

			s2_freq();
			if ((value & 128) != 0)
				s2_init();
			break;
		case 0x0a:

			if ((value & 128) == 0)
				S3.on = 0;
			break;
		case 0x0b:

			S3.len = (256 - value) << 13;
			break;

		case 0x0d:

			s3_freq();
			break;
		case 0x0e:

			s3_freq();
			if ((value & 128) != 0)
				s3_init();
			break;
		case 0x10:

			S4.len = (64 - (value & 63)) << 13;
			break;
		case 0x11:

			S4.envol = value >> 4;
			S4.endir = (value >> 3) & 1;
			S4.endir |= S4.endir - 1;
			S4.enlen = (value & 7) << 15;
			break;
		case 0x12:

			s4_freq();
			break;
		case 0x13:

			if ((value & 128) != 0)
				s4_init();
			break;

		case 0x16:

			if ((value & 128) == 0)
				sound_off();
			break;
		default:
			return;
		}
	}

	final int dmgwave[] = { 0xac, 0xdd, 0xda, 0x48, 0x36, 0x02, 0xcf, 0x16, 0x2c, 0x04, 0xe5, 0x2c, 0xac, 0xdd, 0xda, 0x48 };

	final int cgbwave[] = { 0x00, 0xff, 0x00, 0xff, 0x00, 0xff, 0x00, 0xff, 0x00, 0xff, 0x00, 0xff, 0x00, 0xff, 0x00, 0xff, };

	final int sqwave[][] = { { 0, 0, -1, 0, 0, 0, 0, 0 }, { 0, -1, -1, 0, 0, 0, 0, 0 }, { -1, -1, -1, -1, 0, 0, 0, 0 }, { -1, 0, 0, -1, -1, -1, -1, -1 } };

	final int freqtab[] = { (1 << 14) * 2, (1 << 14), (1 << 14) / 2, (1 << 14) / 3, (1 << 14) / 4, (1 << 14) / 5, (1 << 14) / 6, (1 << 14) / 7 };

	final int noise7[] = { 0xfd, 0xf3, 0xd7, 0x0d, 0xd3, 0x15, 0x82, 0xf1, 0xdb, 0x25, 0x21, 0x39, 0x68, 0x8c, 0xd5, 0x00, };

	final int noise15[] = { 0xff, 0xfd, 0xff, 0xf3, 0xff, 0xd7, 0xff, 0x0f, 0xfd, 0xdf, 0xf3, 0x3f, 0xd5, 0x7f, 0x00, 0xfd, 0xfd, 0xf3, 0xf3, 0xd7, 0xd7, 0x0f, 0x0d, 0xdd, 0xd3, 0x33, 0x15, 0x55, 0x80, 0x02, 0xff, 0xf1, 0xff, 0xdb, 0xff, 0x27, 0xfd, 0x2f, 0xf1, 0x1f, 0xd9, 0xbf, 0x2a, 0x7d, 0x02, 0xf1, 0xf1, 0xdb, 0xdb, 0x27, 0x25, 0x2d, 0x21, 0x11, 0x39, 0x99, 0x6a, 0xa8, 0x80, 0x0c, 0xff, 0xd5, 0xff, 0x03, 0xfd, 0xf7, 0xf3, 0xcf, 0xd7, 0x5f, 0x0c, 0x3d, 0xd7, 0x73, 0x0c, 0xd5, 0xd5, 0x03, 0x01, 0xf5, 0xfb, 0xc3, 0xe7, 0x77, 0xac, 0xce, 0x15, 0x5b, 0x80, 0x26, 0xff, 0x29, 0xfd, 0x0b, 0xf1, 0xc7, 0xdb, 0x6f, 0x24, 0x9d, 0x24, 0xb1, 0x24, 0x59, 0x26, 0x29, 0x2b, 0x09, 0x05, 0xc9, 0xe3, 0x4b, 0xb4, 0x46, 0x46, 0x6a, 0x6a, 0x82, 0x80, 0xf0, 0xfd, 0xdd, 0xf3, 0x33, 0xd5, 0x57, 0x00, 0x0d, 0xff, 0xd3, 0xff, 0x17, 0xfd, 0x8f, 0xf2, 0xdf, 0xd1, 0x3f, 0x19, 0x7d, 0xa8, 0xf2, 0x0d, 0xd3, 0xd3, 0x17, 0x15, 0x8d, 0x82, 0xd2, 0xf1, 0x11, 0xd9, 0x9b, 0x2a, 0xa5, 0x00, 0x21, 0xff, 0x3b, 0xfd, 0x67, 0xf0, 0xaf, 0xdc, 0x1f, 0x37, 0xbd, 0x4e, 0x70, 0x5a, 0xde, 0x21, 0x3b, 0x39, 0x65, 0x68, 0xa0, 0x8c, 0x3c, 0xd7, 0x75, 0x0c, 0xc1, 0xd5, 0x7b, 0x00, 0xe5, 0xfd, 0xa3, 0xf2, 0x37, 0xd3, 0x4f, 0x14, 0x5d, 0x86, 0x32, 0xeb, 0x51, 0x84, 0x1a, 0xe7, 0xa1, 0xae, 0x3a, 0x1b, 0x63, 0xa4, 0xb6, 0x24, 0x4b, 0x26, 0x45, 0x2a, 0x61, 0x02, 0xb9, 0xf0, 0x6b, 0xde, 0x87, 0x38, 0xed, 0x6d, 0x90, 0x92, 0x9c, 0x90, 0xb4, 0x9c, 0x44, 0xb6, 0x64, 0x4a, 0xa6, 0x40, 0x2a, 0x7f, 0x02, 0xfd, 0xf1, 0xf3, 0xdb, 0xd7, 0x27, 0x0d, 0x2d, 0xd1, 0x13, 0x19, 0x95, 0xaa, 0x82, 0x00, 0xf3, 0xfd, 0xd7, 0xf3, 0x0f, 0xd5, 0xdf, 0x03, 0x3d, 0xf5, 0x73, 0xc0, 0xd7, 0x7d, 0x0c, 0xf1, 0xd5, 0xdb, 0x03, 0x25, 0xf5, 0x23, 0xc1, 0x37, 0x79, 0x4c, 0xe8, 0x55, 0x8e, 0x02, 0xdb, 0xf1, 0x27, 0xd9, 0x2f, 0x29, 0x1d, 0x09, 0xb1, 0xca, 0x5b, 0x42, 0x24, 0x73, 0x26, 0xd5, 0x29, 0x01, 0x09, 0xf9, 0xcb, 0xeb, 0x47, 0x84, 0x6e, 0xe6, 0x99, 0xa8, 0xaa, 0x0c, 0x03, 0xd7, 0xf7, 0x0f, 0xcd, 0xdf, 0x53, 0x3c, 0x15, 0x77, 0x80, 0xce, 0xfd, 0x59, 0xf0, 0x2b, 0xdf, 0x07, 0x3d, 0xed, 0x73, 0x90, 0xd6, 0x9d, 0x08, 0xb1, 0xcc, 0x5b, 0x56, 0x24, 0x0b, 0x27, 0xc5, 0x2f, 0x61, 0x1c, 0xb9, 0xb4, 0x6a, 0x46, 0x82, 0x68, 0xf2, 0x8d, 0xd0, 0xd3, 0x1d, 0x15, 0xb1, 0x82, 0x5a, 0xf2, 0x21, 0xd3, 0x3b, 0x15, 0x65, 0x80, 0xa2, 0xfc, 0x31, 0xf7, 0x5b, 0xcc, 0x27, 0x57, 0x2c, 0x0d, 0x17, 0xd1, 0x8f, 0x1a, 0xdd, 0xa1, 0x32, 0x39, 0x53, 0x68, 0x14, 0x8f, 0x84, 0xde, 0xe5, 0x39, 0xa1, 0x6a, 0x38, 0x83, 0x6c, 0xf4, 0x95, 0xc4, 0x83, 0x64, 0xf4, 0xa5, 0xc4, 0x23, 0x67, 0x34, 0xad, 0x44, 0x10, 0x67, 0x9e, 0xae, 0xb8, 0x18, 0x6f, 0xae, 0x9e, 0x18, 0xbb, 0xac, 0x66, 0x16, 0xab, 0x88, 0x06, 0xcf, 0xe9, 0x5f, 0x88, 0x3e, 0xcf, 0x79, 0x5c, 0xe8, 0x35, 0x8f, 0x42, 0xdc, 0x71, 0x36, 0xd9, 0x49, 0x28, 0x49, 0x0e, 0x49, 0xda, 0x4b, 0x22, 0x45, 0x32, 0x61, 0x52, 0xb8, 0x10, 0x6f, 0x9e, 0x9e, 0xb8, 0xb8, 0x6c, 0x6e, 0x96, 0x98, 0x88, 0xac, 0xcc, 0x15, 0x57, 0x80, 0x0e, 0xff, 0xd9, 0xff, 0x2b, 0xfd, 0x07, 0xf1, 0xef, 0xdb, 0x9f, 0x26, 0xbd, 0x28, 0x71, 0x0e, 0xd9, 0xd9, 0x2b, 0x29, 0x05, 0x09, 0xe1, 0xcb, 0xbb, 0x46, 0x64, 0x6a, 0xa6, 0x80, 0x28, 0xff, 0x0d, 0xfd, 0xd3, 0xf3, 0x17, 0xd5, 0x8f, 0x02, 0xdd, 0xf1, 0x33, 0xd9, 0x57, 0x28, 0x0d, 0x0f, 0xd1, 0xdf, 0x1b, 0x3d, 0xa5, 0x72, 0x20, 0xd3, 0x3d, 0x15, 0x71, 0x80, 0xda, 0xfd, 0x21, 0xf1, 0x3b, 0xd9, 0x67, 0x28, 0xad, 0x0c, 0x11, 0xd7, 0x9b, 0x0e, 0xa5, 0xd8, 0x23, 0x2f, 0x35, 0x1d, 0x41, 0xb0, 0x7a, 0x5e, 0xe2, 0x39, 0xb3, 0x6a, 0x54, 0x82, 0x04, 0xf3, 0xe5, 0xd7, 0xa3, 0x0e, 0x35, 0xdb, 0x43, 0x24, 0x75, 0x26, 0xc1, 0x29, 0x79, 0x08, 0xe9, 0xcd, 0x8b, 0x52, 0xc4, 0x11, 0x67, 0x98, 0xae, 0xac, 0x18, 0x17, 0xaf, 0x8e, 0x1e, 0xdb, 0xb9, 0x26, 0x69, 0x2a, 0x89, 0x00, 0xc9, 0xfd, 0x4b, 0xf0, 0x47, 0xde, 0x6f, 0x3a, 0x9d, 0x60, 0xb0, 0xbc, 0x5c, 0x76, 0x36, 0xcb, 0x49, 0x44, 0x48, 0x66, 0x4e, 0xaa, 0x58, 0x02, 0x2f, 0xf3, 0x1f, 0xd5, 0xbf, 0x02, 0x7d, 0xf2, 0xf3, 0xd1, 0xd7, 0x1b, 0x0d, 0xa5, 0xd2, 0x23, 0x13, 0x35, 0x95, 0x42, 0x80, 0x70, 0xfe, 0xdd, 0xf9, 0x33, 0xe9, 0x57, 0x88, 0x0e, 0xcf, 0xd9, 0x5f, 0x28, 0x3d, 0x0f, 0x71, 0xdc, 0xdb, 0x35, 0x25, 0x41, 0x20, 0x79, 0x3e, 0xe9, 0x79, 0x88, 0xea, 0xcd, 0x81, 0x52, 0xf8, 0x11, 0xef, 0x9b, 0x9e, 0xa6, 0xb8, 0x28, 0x6f, 0x0e, 0x9d, 0xd8, 0xb3, 0x2c, 0x55, 0x16, 0x01, 0x8b, 0xfa, 0xc7, 0xe1, 0x6f, 0xb8, 0x9e, 0x6c, 0xba, 0x94, 0x60, 0x86, 0xbc, 0xe8, 0x75, 0x8e, 0xc2, 0xd9, 0x71, 0x28, 0xd9, 0x0d, 0x29, 0xd1, 0x0b, 0x19, 0xc5, 0xab, 0x62, 0x04, 0xb3, 0xe4, 0x57, 0xa6, 0x0e, 0x2b, 0xdb, 0x07, 0x25, 0xed, 0x23, 0x91, 0x36, 0x99, 0x48, 0xa8, 0x4c, 0x0e, 0x57, 0xda, 0x0f, 0x23, 0xdd, 0x37, 0x31, 0x4d, 0x58, 0x50, 0x2e, 0x1f, 0x1b, 0xbd, 0xa6, 0x72, 0x2a, 0xd3, 0x01, 0x15, 0xf9, 0x83, 0xea, 0xf7, 0x81, 0xce, 0xfb, 0x59, 0xe4, 0x2b, 0xa7, 0x06, 0x2d, 0xeb, 0x13, 0x85, 0x96, 0xe2, 0x89, 0xb0, 0xca, 0x5d, 0x42, 0x30, 0x73, 0x5e, 0xd4, 0x39, 0x07, 0x69, 0xec, 0x8b, 0x94, 0xc6, 0x85, 0x68, 0xe0, 0x8d, 0xbc, 0xd2, 0x75, 0x12, 0xc1, 0x91, 0x7a, 0x98, 0xe0, 0xad, 0xbc, 0x12, 0x77, 0x92, 0xce, 0x91, 0x58, 0x98, 0x2c, 0xaf, 0x14, 0x1d, 0x87, 0xb2, 0xee, 0x51, 0x9a, 0x1a, 0xa3, 0xa0, 0x36, 0x3f, 0x4b, 0x7c, 0x44, 0xf6, 0x65, 0xca, 0xa3, 0x40, 0x34, 0x7f, 0x46, 0xfc, 0x69, 0xf6, 0x8b, 0xc8, 0xc7, 0x4d, 0x6c, 0x50, 0x96, 0x1c, 0x8b, 0xb4, 0xc6, 0x45, 0x6a, 0x60, 0x82, 0xbc, 0xf0, 0x75, 0xde, 0xc3, 0x39, 0x75, 0x68, 0xc0, 0x8d, 0x7c, 0xd0, 0xf5, 0x1d, 0xc1, 0xb3, 0x7a, 0x54, 0xe2, 0x05, 0xb3, 0xe2, 0x57, 0xb2, 0x0e, 0x53, 0xda, 0x17, 0x23, 0x8d, 0x36, 0xd1, 0x49, 0x18, 0x49, 0xae, 0x4a, 0x1a, 0x43, 0xa2, 0x76, 0x32, 0xcb, 0x51, 0x44, 0x18, 0x67, 0xae, 0xae, 0x18, 0x1b, 0xaf, 0xa6, 0x1e, 0x2b, 0xbb, 0x06, 0x65, 0xea, 0xa3, 0x80, 0x36, 0xff, 0x49, 0xfc, 0x4b, 0xf6, 0x47, 0xca, 0x6f, 0x42, 0x9c, 0x70, 0xb6, 0xdc, 0x49, 0x36, 0x49, 0x4a, 0x48, 0x42, 0x4e, 0x72, 0x5a, 0xd2, 0x21, 0x13, 0x39, 0x95, 0x6a, 0x80, 0x80, 0xfc, 0xfd, 0xf5, 0xf3, 0xc3, 0xd7, 0x77, 0x0c, 0xcd, 0xd5, 0x53, 0x00, 0x15, 0xff, 0x83, 0xfe, 0xf7, 0xf9, 0xcf, 0xeb, 0x5f, 0x84, 0x3e, 0xe7, 0x79, 0xac, 0xea, 0x15, 0x83, 0x82, 0xf6, 0xf1, 0xc9, 0xdb, 0x4b, 0x24, 0x45, 0x26, 0x61, 0x2a, 0xb9, 0x00, 0x69, 0xfe, 0x8b, 0xf8, 0xc7, 0xed, 0x6f, 0x90, 0x9e, 0x9c, 0xb8, 0xb4, 0x6c, 0x46, 0x96, 0x68, 0x8a, 0x8c, 0xc0, 0xd5, 0x7d, 0x00, 0xf1, 0xfd, 0xdb, 0xf3, 0x27, 0xd5, 0x2f, 0x01, 0x1d, 0xf9, 0xb3, 0xea, 0x57, 0x82, 0x0e, 0xf3, 0xd9, 0xd7, 0x2b, 0x0d, 0x05, 0xd1, 0xe3, 0x1b, 0xb5, 0xa6, 0x42, 0x2a, 0x73, 0x02, 0xd5, 0xf1, 0x03, 0xd9, 0xf7, 0x2b, 0xcd, 0x07, 0x51, 0xec, 0x1b, 0x97, 0xa6, 0x8e, 0x28, 0xdb, 0x0d, 0x25, 0xd1, 0x23, 0x19, 0x35, 0xa9, 0x42, 0x08, 0x73, 0xce, 0xd7, 0x59, 0x0c, 0x29, 0xd7, 0x0b, 0x0d, 0xc5, 0xd3, 0x63, 0x14, 0xb5, 0x84, 0x42, 0xe6, 0x71, 0xaa, 0xda, 0x01, 0x23, 0xf9, 0x37, 0xe9, 0x4f, 0x88, 0x5e, 0xce, 0x39, 0x5b, 0x68, 0x24, 0x8f, 0x24, 0xdd, 0x25, 0x31, 0x21, 0x59, 0x38, 0x29, 0x6f, 0x08, 0x9d, 0xcc, 0xb3, 0x54, 0x54, 0x06, 0x07, 0xeb, 0xef, 0x87, 0x9e, 0xee, 0xb9, 0x98, 0x6a, 0xae, 0x80, 0x18, 0xff, 0xad, 0xfe, 0x13, 0xfb, 0x97, 0xe6, 0x8f, 0xa8, 0xde, 0x0d, 0x3b, 0xd1, 0x67, 0x18, 0xad, 0xac, 0x12, 0x17, 0x93, 0x8e, 0x96, 0xd8, 0x89, 0x2c, 0xc9, 0x15, 0x49, 0x80, 0x4a, 0xfe, 0x41, 0xfa, 0x7b, 0xe2, 0xe7, 0xb1, 0xae, 0x5a, 0x1a, 0x23, 0xa3, 0x36, 0x35, 0x4b, 0x40, 0x44, 0x7e, 0x66, 0xfa, 0xa9, 0xe0, 0x0b, 0xbf, 0xc6, 0x7f, 0x6a, 0xfc, 0x81, 0xf4, 0xfb, 0xc5, 0xe7, 0x63, 0xac, 0xb6, 0x14, 0x4b, 0x86, 0x46, 0xea, 0x69, 0x82, 0x8a, 0xf0, 0xc1, 0xdd, 0x7b, 0x30, 0xe5, 0x5d, 0xa0, 0x32, 0x3f, 0x53, 0x7c, 0x14, 0xf7, 0x85, 0xce, 0xe3, 0x59, 0xb4, 0x2a, 0x47, 0x02, 0x6d, 0xf2, 0x93, 0xd0, 0x97, 0x1c, 0x8d, 0xb4, 0xd2, 0x45, 0x12, 0x61, 0x92, 0xba, 0x90, 0x60, 0x9e, 0xbc, 0xb8, 0x74, 0x6e, 0xc6, 0x99, 0x68, 0xa8, 0x8c, 0x0c, 0xd7, 0xd5, 0x0f, 0x01, 0xdd, 0xfb, 0x33, 0xe5, 0x57, 0xa0, 0x0e, 0x3f, 0xdb, 0x7f, 0x24, 0xfd, 0x25, 0xf1, 0x23, 0xd9, 0x37, 0x29, 0x4d, 0x08, 0x51, 0xce, 0x1b, 0x5b, 0xa4, 0x26, 0x27, 0x2b, 0x2d, 0x05, 0x11, 0xe1, 0x9b, 0xba, 0xa6, 0x60, 0x2a, 0xbf, 0x00, 0x7d, 0xfe, 0xf3, 0xf9, 0xd7, 0xeb, 0x0f, 0x85, 0xde, 0xe3, 0x39, 0xb5, 0x6a, 0x40, 0x82, 0x7c, 0xf2, 0xf5, 0xd1, 0xc3, 0x1b, 0x75, 0xa4, 0xc2, 0x25, 0x73, 0x20, 0xd5, 0x3d, 0x01, 0x71, 0xf8, 0xdb, 0xed, 0x27, 0x91, 0x2e, 0x99, 0x18, 0xa9, 0xac, 0x0a, 0x17, 0xc3, 0x8f, 0x76, 0xdc, 0xc9, 0x35, 0x49, 0x40, 0x48, 0x7e, 0x4e, 0xfa, 0x59, 0xe2, 0x2b, 0xb3, 0x06, 0x55, 0xe2, 0x03, 0x83, 0xf6, 0xf7, 0xc9, 0xcf, 0x4b, 0x5c, 0x04, 0x3e, 0x67, 0x4e, 0xac, 0x60, 0x17, 0x7f, 0x80, 0xfe, 0xc1, 0xf9, 0x7b, 0xe8, 0xe7, 0x87, 0xae, 0xc2, 0x19, 0x93, 0xfc, 0x96, 0x08, 0x8f, 0xc0, 0xe7, 0xfc, 0x2c, 0xf0, 0x1d, 0xcc, 0xc3, 0x9e, 0x70, 0x00, 0xc0, 0x63, 0x7f, 0x54, 0x78, 0x40, 0xfe, 0x61, 0x9b, 0xf3, 0x40, 0x64, 0x3f, 0x0f, 0xf8, 0x2c, 0xf3, 0x3f, 0x99, 0x83, 0x2a, 0x79, 0x07, 0xcb, 0xe1, 0x9f, 0xcc, 0xce, 0x60, 0x6c, 0x00, 0x84, 0x7c, 0x0f, 0xf5, 0xe8, 0xcf, 0x15, 0x66, 0x80, 0xb0, 0xf8, 0x5d, 0xf4, 0x33, 0x8a, 0x57, 0x44, 0x0c, 0x67, 0xd6, 0xaf, 0x08, 0x1f, 0xcf, 0xb3, 0x5e, 0x54, 0x3a, 0x07, 0x63, 0xec, 0xb7, 0x94, 0x4e, 0x86, 0x58, 0xea, 0x2d, 0x83, 0x12, 0xf5, 0x91, 0xc2, 0x9b, 0x70, 0xa4, 0xdc, 0x25, 0x37, 0x21, 0x4d, 0x38, 0x51, 0x6e, 0x18, 0x9b, 0xac, 0xa6, 0x14, 0x2b, 0x87, 0x06, 0xed, 0xe9, 0x93, 0x8a, 0x96, 0xc0, 0x89, 0x7c, 0xc8, 0xf5, 0x4d, 0xc0, 0x53, 0x7e, 0x14, 0xfb, 0x85, 0xe6, 0xe3, 0xa9, 0xb6, 0x0a, 0x4b, 0xc2, 0x47, 0x72, 0x6c, 0xd2, 0x95, 0x10, 0x81, 0x9c, 0xfa, 0xb5, 0xe0, 0x43, 0xbe, 0x76, 0x7a, 0xca, 0xe1, 0x41, 0xb8, 0x7a, 0x6e, 0xe2, 0x99, 0xb0, 0xaa, 0x5c, 0x02, 0x37, 0xf3, 0x4f, 0xd4, 0x5f, 0x06, 0x3d, 0xeb, 0x73, 0x84, 0xd6, 0xe5, 0x09, 0xa1, 0xca, 0x3b, 0x43, 0x64, 0x74, 0xa6, 0xc4, 0x29, 0x67, 0x08, 0xad, 0xcc, 0x13, 0x57, 0x94, 0x0e, 0x87, 0xd8, 0xef, 0x2d, 0x9d, 0x12, 0xb1, 0x90, 0x5a, 0x9e, 0x20, 0xbb, 0x3c, 0x65, 0x76, 0xa0, 0xc8, 0x3d, 0x4f, 0x70, 0x5c, 0xde, 0x35, 0x3b, 0x41, 0x64, 0x78, 0xa6, 0xec, 0x29, 0x97, 0x0a, 0x8d, 0xc0, 0xd3, 0x7d, 0x14, 0xf1, 0x85, 0xda, 0xe3, 0x21, 0xb5, 0x3a, 0x41, 0x62, 0x78, 0xb2, 0xec, 0x51, 0x96, 0x1a, 0x8b, 0xa0, 0xc6, 0x3d, 0x6b, 0x70, 0x84, 0xdc, 0xe5, 0x35, 0xa1, 0x42, 0x38, 0x73, 0x6e, 0xd4, 0x99, 0x04, 0xa9, 0xe4, 0x0b, 0xa7, 0xc6, 0x2f,
			0x6b, 0x1c, 0x85, 0xb4, 0xe2, 0x45, 0xb2, 0x62, 0x52, 0xb2, 0x10, 0x53, 0x9e, 0x16, 0xbb, 0x88, 0x66, 0xce, 0xa9, 0x58, 0x08, 0x2f, 0xcf, 0x1f, 0x5d, 0xbc, 0x32, 0x77, 0x52, 0xcc, 0x11, 0x57, 0x98, 0x0e, 0xaf, 0xd8, 0x1f, 0x2f, 0xbd, 0x1e, 0x71, 0xba, 0xda, 0x61, 0x22, 0xb9, 0x30, 0x69, 0x5e, 0x88, 0x38, 0xcf, 0x6d, 0x5c, 0x90, 0x34, 0x9f, 0x44, 0xbc, 0x64, 0x76, 0xa6, 0xc8, 0x29, 0x4f, 0x08, 0x5d, 0xce, 0x33, 0x5b, 0x54, 0x24, 0x07, 0x27, 0xed, 0x2f, 0x91, 0x1e, 0x99, 0xb8, 0xaa, 0x6c, 0x02, 0x97, 0xf0, 0x8f, 0xdc, 0xdf, 0x35, 0x3d, 0x41, 0x70, 0x78, 0xde, 0xed, 0x39, 0x91, 0x6a, 0x98, 0x80, 0xac, 0xfc, 0x15, 0xf7, 0x83, 0xce, 0xf7, 0x59, 0xcc, 0x2b, 0x57, 0x04, 0x0d, 0xe7, 0xd3, 0xaf, 0x16, 0x1d, 0x8b, 0xb2, 0xc6, 0x51, 0x6a, 0x18, 0x83, 0xac, 0xf6, 0x15, 0xcb, 0x83, 0x46, 0xf4, 0x69, 0xc6, 0x8b, 0x68, 0xc4, 0x8d, 0x64, 0xd0, 0xa5, 0x1c, 0x21, 0xb7, 0x3a, 0x4d, 0x62, 0x50, 0xb2, 0x1c, 0x53, 0xb6, 0x16, 0x4b, 0x8a, 0x46, 0xc2, 0x69, 0x72, 0x88, 0xd0, 0xcd, 0x1d, 0x51, 0xb0, 0x1a, 0x5f, 0xa2, 0x3e, 0x33, 0x7b, 0x54, 0xe4, 0x05, 0xa7, 0xe2, 0x2f, 0xb3, 0x1e, 0x55, 0xba, 0x02, 0x63, 0xf2, 0xb7, 0xd0, 0x4f, 0x1e, 0x5d, 0xba, 0x32, 0x63, 0x52, 0xb4, 0x10, 0x47, 0x9e, 0x6e, 0xba, 0x98, 0x60, 0xae, 0xbc, 0x18, 0x77, 0xae, 0xce, 0x19, 0x5b, 0xa8, 0x26, 0x0f, 0x2b, 0xdd, 0x07, 0x31, 0xed, 0x5b, 0x90, 0x26, 0x9f, 0x28, 0xbd, 0x0c, 0x71, 0xd6, 0xdb, 0x09, 0x25, 0xc9, 0x23, 0x49, 0x34, 0x49, 0x46, 0x48, 0x6a, 0x4e, 0x82, 0x58, 0xf2, 0x2d, 0xd3, 0x13, 0x15, 0x95, 0x82, 0x82, 0xf0, 0xf1, 0xdd, 0xdb, 0x33, 0x25, 0x55, 0x20, 0x01, 0x3f, 0xf9, 0x7f, 0xe8, 0xff, 0x8d, 0xfe, 0xd3, 0xf9, 0x17, 0xe9, 0x8f, 0x8a, 0xde, 0xc1, 0x39, 0x79, 0x68, 0xe8, 0x8d, 0x8c, 0xd2, 0xd5, 0x11, 0x01, 0x99, 0xfa, 0xab, 0xe0, 0x07, 0xbf, 0xee, 0x7f, 0x9a, 0xfe, 0xa1, 0xf8, 0x3b, 0xef, 0x67, 0x9c, 0xae, 0xb4, 0x18, 0x47, 0xae, 0x6e, 0x1a, 0x9b, 0xa0, 0xa6, 0x3c, 0x2b, 0x77, 0x04, 0xcd, 0xe5, 0x53, 0xa0, 0x16, 0x3f, 0x8b, 0x7e, 0xc4, 0xf9, 0x65, 0xe8, 0xa3, 0x8c, 0x36, 0xd7, 0x49, 0x0c, 0x49, 0xd6, 0x4b, 0x0a, 0x45, 0xc2, 0x63, 0x72, 0xb4, 0xd0, 0x45, 0x1e, 0x61, 0xba, 0xba, 0x60, 0x62, 0xbe, 0xb0, 0x78, 0x5e, 0xee, 0x39, 0x9b, 0x6a, 0xa4, 0x80, 0x24, 0xff, 0x25, 0xfd, 0x23, 0xf1, 0x37, 0xd9, 0x4f, 0x28, 0x5d, 0x0e, 0x31, 0xdb, 0x5b, 0x24, 0x25, 0x27, 0x21, 0x2d, 0x39, 0x11, 0x69, 0x98, 0x8a, 0xac, 0xc0, 0x15, 0x7f, 0x80, 0xfe, 0xfd, 0xf9, 0xf3, 0xeb, 0xd7, 0x87, 0x0e, 0xed, 0xd9, 0x93, 0x2a, 0x95, 0x00, 0x81, 0xfc, 0xfb, 0xf5, 0xe7, 0xc3, 0xaf, 0x76, 0x1c, 0xcb, 0xb5, 0x46, 0x40, 0x6a, 0x7e, 0x82, 0xf8, 0xf1, 0xed, 0xdb, 0x93, 0x26, 0x95, 0x28, 0x81, 0x0c, 0xf9, 0xd5, 0xeb, 0x03, 0x85, 0xf6, 0xe3, 0xc9, 0xb7, 0x4a, 0x4c, 0x42, 0x56, 0x72, 0x0a, 0xd3, 0xc1, 0x17, 0x79, 0x8c, 0xea, 0xd5, 0x81, 0x02, 0xf9, 0xf1, 0xeb, 0xdb, 0x87, 0x26, 0xed, 0x29, 0x91, 0x0a, 0x99, 0xc0, 0xab, 0x7c, 0x04, 0xf7, 0xe5, 0xcf, 0xa3, 0x5e, 0x34, 0x3b, 0x47, 0x64, 0x6c, 0xa6, 0x94, 0x28, 0x87, 0x0c, 0xed, 0xd5, 0x93, 0x02, 0x95, 0xf0, 0x83, 0xdc, 0xf7, 0x35, 0xcd, 0x43, 0x50, 0x74, 0x1e, 0xc7, 0xb9, 0x6e, 0x68, 0x9a, 0x8c, 0xa0, 0xd4, 0x3d, 0x07, 0x71, 0xec, 0xdb, 0x95, 0x26, 0x81, 0x28, 0xf9, 0x0d, 0xe9, 0xd3, 0x8b, 0x16, 0xc5, 0x89, 0x62, 0xc8, 0xb1, 0x4c, 0x58, 0x56, 0x2e, 0x0b, 0x1b, 0xc5, 0xa7, 0x62, 0x2c, 0xb3, 0x14, 0x55, 0x86, 0x02, 0xeb, 0xf1, 0x87, 0xda, 0xef, 0x21, 0x9d, 0x3a, 0xb1, 0x60, 0x58, 0xbe, 0x2c, 0x7b, 0x16, 0xe5, 0x89, 0xa2, 0xca, 0x31, 0x43, 0x58, 0x74, 0x2e, 0xc7, 0x19, 0x6d, 0xa8, 0x92, 0x0c, 0x93, 0xd4, 0x97, 0x04, 0x8d, 0xe4, 0xd3, 0xa5, 0x16, 0x21, 0x8b, 0x3a, 0xc5, 0x61, 0x60, 0xb8, 0xbc, 0x6c, 0x76, 0x96, 0xc8, 0x89, 0x4c, 0xc8, 0x55, 0x4e, 0x00, 0x5b, 0xfe, 0x27, 0xfb, 0x2f, 0xe5, 0x1f, 0xa1, 0xbe, 0x3a, 0x7b, 0x62, 0xe4, 0xb1, 0xa4, 0x5a, 0x26, 0x23, 0x2b, 0x35, 0x05, 0x41, 0xe0, 0x7b, 0xbe, 0xe6, 0x79, 0xaa, 0xea, 0x01, 0x83, 0xfa, 0xf7, 0xe1, 0xcf, 0xbb, 0x5e, 0x64, 0x3a, 0xa7, 0x60, 0x2c, 0xbf, 0x14, 0x7d, 0x86, 0xf2, 0xe9, 0xd1, 0x8b, 0x1a, 0xc5, 0xa1, 0x62, 0x38, 0xb3, 0x6c, 0x54, 0x96, 0x04, 0x8b, 0xe4, 0xc7, 0xa5, 0x6e, 0x20, 0x9b, 0x3c, 0xa5, 0x74, 0x20, 0xc7, 0x3d, 0x6d, 0x70, 0x90, 0xdc, 0x9d, 0x34, 0xb1, 0x44, 0x58, 0x66, 0x2e, 0xab, 0x18, 0x05, 0xaf, 0xe2, 0x1f, 0xb3, 0xbe, 0x56, 0x7a, 0x0a, 0xe3, 0xc1, 0xb7, 0x7a, 0x4c, 0xe2, 0x55, 0xb2, 0x02, 0x53, 0xf2, 0x17, 0xd3, 0x8f, 0x16, 0xdd, 0x89, 0x32, 0xc9, 0x51, 0x48, 0x18, 0x4f, 0xae, 0x5e, 0x1a, 0x3b, 0xa3, 0x66, 0x34, 0xab, 0x44, 0x04, 0x67, 0xe6, 0xaf, 0xa8, 0x1e, 0x0f, 0xbb, 0xde, 0x67, 0x3a, 0xad, 0x60, 0x10, 0xbf, 0x9c, 0x7e, 0xb6, 0xf8, 0x49, 0xee, 0x4b, 0x9a, 0x46, 0xa2, 0x68, 0x32, 0x8f, 0x50, 0xdc, 0x1d, 0x37, 0xb1, 0x4e, 0x58, 0x5a, 0x2e, 0x23, 0x1b, 0x35, 0xa5, 0x42, 0x20, 0x73, 0x3e, 0xd5, 0x79, 0x00, 0xe9, 0xfd, 0x8b, 0xf2, 0xc7, 0xd1, 0x6f, 0x18, 0x9d, 0xac, 0xb2, 0x14, 0x53, 0x86, 0x16, 0xeb, 0x89, 0x86, 0xca, 0xe9, 0x41, 0x88, 0x7a, 0xce, 0xe1, 0x59, 0xb8, 0x2a, 0x6f, 0x02, 0x9d, 0xf0, 0xb3, 0xdc, 0x57, 0x36, 0x0d, 0x4b, 0xd0, 0x47, 0x1e, 0x6d, 0xba, 0x92, 0x60, 0x92, 0xbc, 0x90, 0x74, 0x9e, 0xc4, 0xb9, 0x64, 0x68, 0xa6, 0x8c, 0x28, 0xd7, 0x0d, 0x0d, 0xd1, 0xd3, 0x1b, 0x15, 0xa5, 0x82, 0x22, 0xf3, 0x31, 0xd5, 0x5b, 0x00, 0x25, 0xff, 0x23, 0xfd, 0x37, 0xf1, 0x4f, 0xd8, 0x5f, 0x2e, 0x3d, 0x1b, 0x71, 0xa4, 0xda, 0x25, 0x23, 0x21, 0x35, 0x39, 0x41, 0x68, 0x78, 0x8e, 0xec, 0xd9, 0x95, 0x2a, 0x81, 0x00, 0xf9, 0xfd, 0xeb, 0xf3, 0x87, 0xd6, 0xef, 0x09, 0x9d, 0xca, 0xb3, 0x40, 0x54, 0x7e, 0x06, 0xfb, 0xe9, 0xe7, 0x8b, 0xae, 0xc6, 0x19, 0x6b, 0xa8, 0x86, 0x0c, 0xeb, 0xd5, 0x87, 0x02, 0xed, 0xf1, 0x93, 0xda, 0x97, 0x20, 0x8d, 0x3c, 0xd1, 0x75, 0x18, 0xc1, 0xad, 0x7a, 0x10, 0xe3, 0x9d, 0xb6, 0xb2, 0x48, 0x52, 0x4e, 0x12, 0x5b, 0x92, 0x26, 0x93, 0x28, 0x95, 0x0c, 0x81, 0xd4, 0xfb, 0x05, 0xe5, 0xe3, 0xa3, 0xb6, 0x36, 0x4b, 0x4a, 0x44, 0x42, 0x66, 0x72, 0xaa, 0xd0, 0x01, 0x1f, 0xf9, 0xbf, 0xea, 0x7f, 0x82, 0xfe, 0xf1, 0xf9, 0xdb, 0xeb, 0x27, 0x85, 0x2e, 0xe1, 0x19, 0xb9, 0xaa, 0x6a, 0x02, 0x83, 0xf0, 0xf7, 0xdd, 0xcf, 0x33, 0x5d, 0x54, 0x30, 0x07, 0x5f, 0xec, 0x3f, 0x97, 0x7e, 0x8c, 0xf8, 0xd5, 0xed, 0x03, 0x91, 0xf6, 0x9b, 0xc8, 0xa7, 0x4c, 0x2c, 0x57, 0x16, 0x0d, 0x8b, 0xd2, 0xc7, 0x11, 0x6d, 0x98, 0x92, 0xac, 0x90, 0x14, 0x9f, 0x84, 0xbe, 0xe4, 0x79, 0xa6, 0xea, 0x29, 0x83, 0x0a, 0xf5, 0xc1, 0xc3, 0x7b, 0x74, 0xe4, 0xc5, 0xa5, 0x62, 0x20, 0xb3, 0x3c, 0x55, 0x76, 0x00, 0xcb, 0xfd, 0x47, 0xf0, 0x6f, 0xde, 0x9f, 0x38, 0xbd, 0x6c, 0x70, 0x96, 0xdc, 0x89, 0x34, 0xc9, 0x45, 0x48, 0x60, 0x4e, 0xbe, 0x58, 0x7a, 0x2e, 0xe3, 0x19, 0xb5, 0xaa, 0x42, 0x02, 0x73, 0xf2, 0xd7, 0xd1, 0x0f, 0x19, 0xdd, 0xab, 0x32, 0x05, 0x53, 0xe0, 0x17, 0xbf, 0x8e, 0x7e, 0xda, 0xf9, 0x21, 0xe9, 0x3b, 0x89, 0x66, 0xc8, 0xa9, 0x4c, 0x08, 0x57, 0xce, 0x0f, 0x5b, 0xdc, 0x27, 0x37, 0x2d, 0x4d, 0x10, 0x51, 0x9e, 0x1a, 0xbb, 0xa0, 0x66, 0x3e, 0xab, 0x78, 0x04, 0xef, 0xe5, 0x9f, 0xa2, 0xbe, 0x30, 0x7b, 0x5e, 0xe4, 0x39, 0xa7, 0x6a, 0x2c, 0x83, 0x14, 0xf5, 0x85, 0xc2, 0xe3, 0x71, 0xb4, 0xda, 0x45, 0x22, 0x61, 0x32, 0xb9, 0x50, 0x68, 0x1e, 0x8f, 0xb8, 0xde, 0x6d, 0x3a, 0x91, 0x60, 0x98, 0xbc, 0xac, 0x74, 0x16, 0xc7, 0x89, 0x6e, 0xc8, 0x99, 0x4c, 0xa8, 0x54, 0x0e, 0x07, 0xdb, 0xef, 0x27, 0x9d, 0x2e, 0xb1, 0x18, 0x59, 0xae, 0x2a, 0x1b, 0x03, 0xa5, 0xf6, 0x23, 0xcb, 0x37, 0x45, 0x4c, 0x60, 0x56, 0xbe, 0x08, 0x7b, 0xce, 0xe7, 0x59, 0xac, 0x2a, 0x17, 0x03, 0x8d, 0xf6, 0xd3, 0xc9, 0x17, 0x49, 0x8c, 0x4a, 0xd6, 0x41, 0x0a, 0x79, 0xc2, 0xeb, 0x71, 0x84, 0xda, 0xe5, 0x21, 0xa1, 0x3a, 0x39, 0x63, 0x68, 0xb4, 0x8c, 0x44, 0xd6, 0x65, 0x0a, 0xa1, 0xc0, 0x3b, 0x7f, 0x64, 0xfc, 0xa5, 0xf4, 0x23, 0xc7, 0x37, 0x6d, 0x4c, 0x90, 0x54, 0x9e, 0x04, 0xbb, 0xe4, 0x67, 0xa6, 0xae, 0x28, 0x1b, 0x0f, 0xa5, 0xde, 0x23, 0x3b, 0x35, 0x65, 0x40, 0xa0, 0x7c, 0x3e, 0xf7, 0x79, 0xcc, 0xeb, 0x55, 0x84, 0x02, 0xe7, 0xf1, 0xaf, 0xda, 0x1f, 0x23, 0xbd, 0x36, 0x71, 0x4a, 0xd8, 0x41, 0x2e, 0x79, 0x1a, 0xe9, 0xa1, 0x8a, 0x3a, 0xc3, 0x61, 0x74, 0xb8, 0xc4, 0x6d, 0x66, 0x90, 0xa8, 0x9c, 0x0c, 0xb7, 0xd4, 0x4f, 0x06, 0x5d, 0xea, 0x33, 0x83, 0x56, 0xf4, 0x09, 0xc7, 0xcb, 0x6f, 0x44, 0x9c, 0x64, 0xb6, 0xa4, 0x48, 0x26, 0x4f, 0x2a, 0x5d, 0x02, 0x31, 0xf3, 0x5b, 0xd4, 0x27, 0x07, 0x2d, 0xed, 0x13, 0x91, 0x96, 0x9a, 0x88, 0xa0, 0xcc, 0x3d, 0x57, 0x70, 0x0c, 0xdf, 0xd5, 0x3f, 0x01, 0x7d, 0xf8, 0xf3, 0xed, 0xd7, 0x93, 0x0e, 0x95, 0xd8, 0x83, 0x2c, 0xf5, 0x15, 0xc1, 0x83, 0x7a, 0xf4, 0xe1, 0xc5, 0xbb, 0x62, 0x64, 0xb2, 0xa4, 0x50, 0x26, 0x1f, 0x2b, 0xbd, 0x06, 0x71, 0xea, 0xdb, 0x81, 0x26, 0xf9, 0x29, 0xe9, 0x0b, 0x89, 0xc6, 0xcb, 0x69, 0x44, 0x88, 0x64, 0xce, 0xa5, 0x58, 0x20, 0x2f, 0x3f, 0x1d, 0x7d, 0xb0, 0xf2, 0x5d, 0xd2, 0x33, 0x13, 0x55, 0x94, 0x02, 0x87, 0xf0, 0xef, 0xdd, 0x9f, 0x32, 0xbd, 0x50, 0x70, 0x1e, 0xdf, 0xb9, 0x3e, 0x69, 0x7a, 0x88, 0xe0, 0xcd, 0xbd, 0x52, 0x70, 0x12, 0xdf, 0x91, 0x3e, 0x99, 0x78, 0xa8, 0xec, 0x0d, 0x97, 0xd2, 0x8f, 0x10, 0xdd, 0x9d, 0x32, 0xb1, 0x50, 0x58, 0x1e, 0x2f, 0xbb, 0x1e, 0x65, 0xba, 0xa2, 0x60, 0x32, 0xbf, 0x50, 0x7c, 0x1e, 0xf7, 0xb9, 0xce, 0x6b, 0x5a, 0x84, 0x20, 0xe7, 0x3d, 0xad, 0x72, 0x10, 0xd3, 0x9d, 0x16, 0xb1, 0x88, 0x5a, 0xce, 0x21, 0x5b, 0x38, 0x25, 0x6f, 0x20, 0x9d, 0x3c, 0xb1, 0x74, 0x58, 0xc6, 0x2d, 0x6b, 0x10, 0x85, 0x9c, 0xe2, 0xb5, 0xb0, 0x42, 0x5e, 0x72, 0x3a, 0xd3, 0x61, 0x14, 0xb9, 0x84, 0x6a, 0xe6, 0x81, 0xa8, 0xfa, 0x0d, 0xe3, 0xd3, 0xb7, 0x16, 0x4d, 0x8a, 0x52, 0xc2, 0x11, 0x73, 0x98, 0xd6, 0xad, 0x08, 0x11, 0xcf, 0x9b, 0x5e, 0xa4, 0x38, 0x27, 0x6f, 0x2c, 0x9d, 0x14, 0xb1, 0x84, 0x5a, 0xe6, 0x21, 0xab, 0x3a, 0x05, 0x63, 0xe0, 0xb7, 0xbc, 0x4e, 0x76, 0x5a, 0xca, 0x21, 0x43, 0x38, 0x75, 0x6e, 0xc0, 0x99, 0x7c, 0xa8, 0xf4, 0x0d, 0xc7, 0xd3, 0x6f, 0x14, 0x9d, 0x84, 0xb2, 0xe4, 0x51, 0xa6, 0x1a, 0x2b, 0xa3, 0x06, 0x35, 0xeb, 0x43, 0x84, 0x76, 0xe6, 0xc9, 0xa9, 0x4a, 0x08, 0x43, 0xce, 0x77, 0x5a, 0xcc, 0x21, 0x57, 0x38, 0x0d, 0x6f, 0xd0, 0x9f, 0x1c, 0xbd, 0xb4, 0x72, 0x46, 0xd2, 0x69, 0x12, 0x89, 0x90, 0xca, 0x9d, 0x40, 0xb0, 0x7c, 0x5e, 0xf6, 0x39, 0xcb, 0x6b, 0x44, 0x84, 0x64, 0xe6, 0xa5, 0xa8, 0x22, 0x0f, 0x33, 0xdd, 0x57, 0x30, 0x0d, 0x5f, 0xd0, 0x3f, 0x1f, 0x7d, 0xbc, 0xf2, 0x75, 0xd2, 0xc3, 0x11, 0x75, 0x98, 0xc2, 0xad, 0x70, 0x10, 0xdf, 0x9d, 0x3e, 0xb1, 0x78, 0x58, 0xee, 0x2d, 0x9b, 0x12,
			0xa5, 0x90, 0x22, 0x9f, 0x30, 0xbd, 0x5c, 0x70, 0x36, 0xdf, 0x49, 0x3c, 0x49, 0x76, 0x48, 0xca, 0x4d, 0x42, 0x50, 0x72, 0x1e, 0xd3, 0xb9, 0x16, 0x69, 0x8a, 0x8a, 0xc0, 0xc1, 0x7d, 0x78, 0xf0, 0xed, 0xdd, 0x93, 0x32, 0x95, 0x50, 0x80, 0x1c, 0xff, 0xb5, 0xfe, 0x43, 0xfa, 0x77, 0xe2, 0xcf, 0xb1, 0x5e, 0x58, 0x3a, 0x2f, 0x63, 0x1c, 0xb5, 0xb4, 0x42, 0x46, 0x72, 0x6a, 0xd2, 0x81, 0x10, 0xf9, 0x9d, 0xea, 0xb3, 0x80, 0x56, 0xfe, 0x09, 0xfb, 0xcb, 0xe7, 0x47, 0xac, 0x6e, 0x16, 0x9b, 0x88, 0xa6, 0xcc, 0x29, 0x57, 0x08, 0x0d, 0xcf, 0xd3, 0x5f, 0x14, 0x3d, 0x87, 0x72, 0xec, 0xd1, 0x95, 0x1a, 0x81, 0xa0, 0xfa, 0x3d, 0xe3, 0x73, 0xb4, 0xd6, 0x45, 0x0a, 0x61, 0xc2, 0xbb, 0x70, 0x64, 0xde, 0xa5, 0x38, 0x21, 0x6f, 0x38, 0x9d, 0x6c, 0xb0, 0x94, 0x5c, 0x86, 0x3e, 0xeb, 0x45, 0x84, 0x62, 0xe6, 0xb1, 0xa8, 0x5a, 0x0e, 0x23, 0xfb, 0x33, 0x25, 0x47, 0x20, 0x51, 0x3e, 0x19, 0x7f, 0xa8, 0x66, 0x0c, 0xfb, 0xd0, 0x07, 0x13, 0xe5, 0x9f, 0x83, 0xce, 0x98, 0x58, 0xcd, 0x2e, 0x19, 0x14, 0x39, 0x86, 0x3f, 0xff, 0x01, 0x85, 0xff, 0xe1, 0xe1, 0xb3, 0xfc, 0x46, 0x63, 0x0f, 0xf8, 0x00, 0x53, 0xbe, 0x1f, 0xfb, 0xc0, 0xe6, 0x7e, 0xbc, 0xf0, 0x01, 0xe3, 0xc3, 0x9f, 0xa6, 0xcc, 0x48, 0x7e, 0x40, 0x82, 0x9d, 0xf2, 0xff, 0xd6, 0x07, 0x13, 0xf5, 0x87, 0x80, 0x0f, 0x71, 0x9c, 0xfd, 0x35, 0x61, 0x43, 0xf8, 0x78, 0x7e, 0xcf, 0x19, 0x99, 0xa8, 0x32, 0x00, 0x53, 0xfc, 0x17, 0xfb, 0x8f, 0xc6, 0xdf, 0xa9, 0x3e, 0x09, 0x7b, 0xc8, 0xe7, 0x4d, 0xac, 0x52, 0x16, 0x13, 0x8b, 0x96, 0xc6, 0x89, 0x68, 0xc8, 0x8d, 0x4c, 0xd0, 0x55, 0x1e, 0x01, 0xbb, 0xfa, 0x67, 0xe2, 0xaf, 0xb0, 0x1e, 0x5f, 0xba, 0x3e, 0x63, 0x7a, 0xb4, 0xe0, 0x45, 0xbe, 0x62, 0x7a, 0xb2, 0xe0, 0x51, 0xbe, 0x1a, 0x7b, 0xa2, 0xe6, 0x31, 0xab, 0x5a, 0x04, 0x23, 0xe7, 0x37, 0xad, 0x4e, 0x10, 0x5b, 0x9e, 0x26, 0xbb, 0x28, 0x65, 0x0e, 0xa1, 0xd8, 0x3b, 0x2f, 0x65, 0x1c, 0xa1, 0xb4, 0x3a, 0x47, 0x62, 0x6c, 0xb2, 0x94, 0x50, 0x86, 0x1c, 0xeb, 0xb5, 0x86, 0x42, 0xea, 0x71, 0x82, 0xda, 0xf1, 0x21, 0xd9, 0x3b, 0x29, 0x65, 0x08, 0xa1, 0xcc, 0x3b, 0x57, 0x64, 0x0c, 0xa7, 0xd4, 0x2f, 0x07, 0x1d, 0xed, 0xb3, 0x92, 0x56, 0x92, 0x08, 0x93, 0xcc, 0x97, 0x54, 0x8c, 0x04, 0xd7, 0xe5, 0x0f, 0xa1, 0xde, 0x3b, 0x3b, 0x65, 0x64, 0xa0, 0xa4, 0x3c, 0x27, 0x77, 0x2c, 0xcd, 0x15, 0x51, 0x80, 0x1a, 0xff, 0xa1, 0xfe, 0x3b, 0xfb, 0x67, 0xe4, 0xaf, 0xa4, 0x1e, 0x27, 0xbb, 0x2e, 0x65, 0x1a, 0xa1, 0xa0, 0x3a, 0x3f, 0x63, 0x7c, 0xb4, 0xf4, 0x45, 0xc6, 0x63, 0x6a, 0xb4, 0x80, 0x44, 0xfe, 0x65, 0xfa, 0xa3, 0xe0, 0x37, 0xbf, 0x4e, 0x7c, 0x5a, 0xf6, 0x21, 0xcb, 0x3b, 0x45, 0x64, 0x60, 0xa6, 0xbc, 0x28, 0x77, 0x0e, 0xcd, 0xd9, 0x53, 0x28, 0x15, 0x0f, 0x81, 0xde, 0xfb, 0x39, 0xe5, 0x6b, 0xa0, 0x86, 0x3c, 0xeb, 0x75, 0x84, 0xc2, 0xe5, 0x71, 0xa0, 0xda, 0x3d, 0x23, 0x71, 0x34, 0xd9, 0x45, 0x28, 0x61, 0x0e, 0xb9, 0xd8, 0x6b, 0x2e, 0x85, 0x18, 0xe1, 0xad, 0xba, 0x12, 0x63, 0x92, 0xb6, 0x90, 0x48, 0x9e, 0x4c, 0xba, 0x54, 0x62, 0x06, 0xb3, 0xe8, 0x57, 0x8e, 0x0e, 0xdb, 0xd9, 0x27, 0x29, 0x2d, 0x09, 0x11, 0xc9, 0x9b, 0x4a, 0xa4, 0x40, 0x26, 0x7f, 0x2a, 0xfd, 0x01, 0xf1, 0xfb, 0xdb, 0xe7, 0x27, 0xad, 0x2e, 0x11, 0x1b, 0x99, 0xa6, 0xaa, 0x28, 0x03, 0x0f, 0xf5, 0xdf, 0xc3, 0x3f, 0x75, 0x7c, 0xc0, 0xf5, 0x7d, 0xc0, 0xf3, 0x7d, 0xd4, 0xf3, 0x05, 0xd5, 0xe3, 0x03, 0xb5, 0xf6, 0x43, 0xca, 0x77, 0x42, 0xcc, 0x71, 0x56, 0xd8, 0x09, 0x2f, 0xc9, 0x1f, 0x49, 0xbc, 0x4a, 0x76, 0x42, 0xca, 0x71, 0x42, 0xd8, 0x71, 0x2e, 0xd9, 0x19, 0x29, 0xa9, 0x0a, 0x09, 0xc3, 0xcb, 0x77, 0x44, 0xcc, 0x65, 0x56, 0xa0, 0x08, 0x3f, 0xcf, 0x7f, 0x5c, 0xfc, 0x35, 0xf7, 0x43, 0xcc, 0x77, 0x56, 0xcc, 0x09, 0x57, 0xc8, 0x0f, 0x4f, 0xdc, 0x5f, 0x36, 0x3d, 0x4b, 0x70, 0x44, 0xde, 0x65, 0x3a, 0xa1, 0x60, 0x38, 0xbf, 0x6c, 0x7c, 0x96, 0xf4, 0x89, 0xc4, 0xcb, 0x65, 0x44, 0xa0, 0x64, 0x3e, 0xa7, 0x78, 0x2c, 0xef, 0x15, 0x9d, 0x82, 0xb2, 0xf0, 0x51, 0xde, 0x1b, 0x3b, 0xa5, 0x66, 0x20, 0xab, 0x3c, 0x05, 0x77, 0xe0, 0xcf, 0xbd, 0x5e, 0x70, 0x3a, 0xdf, 0x61, 0x3c, 0xb9, 0x74, 0x68, 0xc6, 0x8d, 0x68, 0xd0, 0x8d, 0x1c, 0xd1, 0xb5, 0x1a, 0x41, 0xa2, 0x7a, 0x32, 0xe3, 0x51, 0xb4, 0x1a, 0x47, 0xa2, 0x6e, 0x32, 0x9b, 0x50, 0xa4, 0x1c, 0x27, 0xb7, 0x2e, 0x4d, 0x1a, 0x51, 0xa2, 0x1a, 0x33, 0xa3, 0x56, 0x34, 0x0b, 0x47, 0xc4, 0x6f, 0x66, 0x9c, 0xa8, 0xb4, 0x0c, 0x47, 0xd6, 0x6f, 0x0a, 0x9d, 0xc0, 0xb3, 0x7c, 0x54, 0xf6, 0x05, 0xcb, 0xe3, 0x47, 0xb4, 0x6e, 0x46, 0x9a, 0x68, 0xa2, 0x8c, 0x30, 0xd7, 0x5d, 0x0c, 0x31, 0xd7, 0x5b, 0x0c, 0x25, 0xd7, 0x23, 0x0d, 0x35, 0xd1, 0x43, 0x18, 0x75, 0xae, 0xc2, 0x19, 0x73, 0xa8, 0xd6, 0x0d, 0x0b, 0xd1, 0xc7, 0x1b, 0x6d, 0xa4, 0x92, 0x24, 0x93, 0x24, 0x95, 0x24, 0x81, 0x24, 0xf9, 0x25, 0xe9, 0x23, 0x89, 0x36, 0xc9, 0x49, 0x48, 0x48, 0x4e, 0x4e, 0x5a, 0x5a, 0x22, 0x23, 0x33, 0x35, 0x55, 0x40, 0x00, };

}
