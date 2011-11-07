package	com.robbix.mp5.basics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A float-based audio data buffer designed to stand-in for JavaSound's byte[] idiom.
 * 
 * Not thread safe.
 */
public class SampleBuffer implements Cloneable
{
	private static final int NS = AudioSystem.NOT_SPECIFIED;
	
	public static SampleBuffer load(AudioInputStream stream) throws IOException
	{
		AudioFormat format = stream.getFormat();
		
		if (! isPCM(format))
			throw new IllegalArgumentException("Must be PCM");
		
		int byteSize = (int) (format.getFrameSize() * stream.getFrameLength());
		byte[] data = new byte[byteSize];
		int bytesRead = 0;
		int pos = 0;
		
		while ((bytesRead = stream.read(data, pos, byteSize - pos)) >= 0)
			pos += bytesRead;
		
		return new SampleBuffer(data, format);
	}
	
	public static SampleBuffer load(File file) throws IOException
	{
		AudioInputStream stream = null;
		
		try
		{
			stream = AudioSystem.getAudioInputStream(file);
			return load(stream);
		}
		catch (UnsupportedAudioFileException uafe)
		{
			throw new FileFormatException(file, uafe.getMessage());
		}
		finally
		{
			if (stream != null)
				stream.close();
		}
	}
	
	/**
	 * When dithering mode is AUTO, it will generally only be done when
	 * sample size is decreased.
	 */
	public static enum DitherMode { AUTO, ON, OFF }
	
	private List<float[]> channelList;
	private int           sampleCount;
	private float         sampleRate;
	private SampleType    originalFormatType;
	private DitherMode    ditherMode = DitherMode.AUTO;
	private float         ditherBits = 0.8f;
	
	/*------------------------------------------------------------------------------------------[*]
	 * Initializers.
	 */
	
	public SampleBuffer(AudioFormat format, int size)
	{
		if (format.getSampleRate() == NS || format.getChannels() == NS)
			throw new IllegalArgumentException("Rate and channels must be specified");
		
		sampleRate  = format.getSampleRate();
		sampleCount = size;
		channelList = newChannels(format.getChannels(), size);
	}
	
	public SampleBuffer(int channels, int size, float rate)
	{
		sampleRate  = rate;
		sampleCount = size;
		channelList = newChannels(channels, size);
	}
	
	public SampleBuffer(byte[] data, AudioFormat format)
	{
		this(data, 0, data.length, format);
	}
	
	public SampleBuffer(byte[] data, int off, int len, AudioFormat format)
	{
		if (! isPCM(format))
			throw new IllegalArgumentException("Must be PCM");
		
		if (off + len > data.length)
			throw new IllegalArgumentException("off + len > data.length");
		
		if (! isFullySpecified(format))
			throw new IllegalArgumentException("Format must be fully specified");
		
		int bytesPerSample = format.getSampleSizeInBits() / 8;
		int bytesPerFrame = bytesPerSample * format.getChannels();
		int size = len / bytesPerFrame;
		channelList = newChannels(format.getChannels(), size);
		sampleRate = format.getSampleRate();
		sampleCount = size;
		SampleType formatType = SampleType.get(format);
		originalFormatType = formatType;
		
		for (int ch=0; ch<format.getChannels(); ch++, off+=bytesPerSample)
			convertByteToFloat(
				data,
				off,
				sampleCount,
				getChannel(ch),
				bytesPerFrame,
				formatType
			);
	}
	
	private SampleBuffer()
	{
	}
	
	/**
	 * Alternative to clone() because of clone's weirdness.
	 */
	public SampleBuffer copy()
	{
		SampleBuffer copy = new SampleBuffer();
		copy.sampleRate = sampleRate;
		copy.sampleCount = sampleCount;
		copy.channelList = copy(channelList, sampleCount);
		return copy;
	}
	
	public SampleBuffer clone()
	{
		return copy();
	}
	
	private List<float[]> newChannels(int channelCount, int length)
	{
		List<float[]> newList = new ArrayList<float[]>(channelCount);
		
		for (int ch = 0; ch < channelCount; ++ch)
			newList.add(new float[length]);
		
		return newList;
	}
	
	private List<float[]> copy(List<float[]> channels, int length)
	{
		List<float[]> newList = new ArrayList<float[]>(channels.size());
		
		for (float[] array : channels)
			newList.add(Arrays.copyOf(array, length));
		
		return newList;
	}
	
	/*------------------------------------------------------------------------------------------[*]
	 * Conversion to byte array.
	 */
	
	/**
	 * Computes the size in bytes of the first numSamples worth
	 * of data in this buffer for the given raw (PCM) audio format.
	 * Format must be PCM_SIGNED or PCM_UNSIGNED.
	 */
	public int getByteSize(AudioFormat format, int numSamples)
	{
		if (! isPCM(format))
			throw new IllegalArgumentException("Format must be PCM");
		
		int bytesPerSample = format.getSampleSizeInBits() / 8;
		int bytesPerFrame = bytesPerSample * format.getChannels();
		float rateRatio = format.getSampleRate() / sampleRate;
		int size = (int) (bytesPerFrame * rateRatio * numSamples);
		return round(size, bytesPerFrame);
	}
	
	/**
	 * Computes the size in bytes of the data in this buffer
	 * for the given raw (PCM) audio format. Format must be
	 * PCM_SIGNED or PCM_UNSIGNED.
	 */
	public int getByteSize(AudioFormat format)
	{
		return getByteSize(format, sampleCount);
	}
	
	/**
	 * Writes byte data of this buffer to given array, starting at off.
	 * Samples are converted to byte data considering format.
	 */
	public int getBytes(byte[] data, AudioFormat format, int numSamples)
	{
		if (! isPCM(format))
			throw new IllegalArgumentException("Must be PCM");
		
		int byteSize = getByteSize(format, numSamples);
		
		if (byteSize > data.length)
			throw new IllegalArgumentException("off + byteSize > data.length");
		
		List<float[]> channels = channelList;
		
		// Spread/downmix channels first if downmixing
		if (! matchChannelCount(format, channels.size()) && format.getChannels() < channels.size())
		{
			if (channels == channelList)
				channels = copy(channelList, sampleCount);
			
			convertChannelCount(channels, format.getChannels());
		}
		
		// Convert sample rate after downmix so we resample less data
		if (! matchSampleRate(format, sampleRate))
		{
			if (channels == channelList)
				channels = copy(channelList, sampleCount);
			
			numSamples = convertSampleRate(channels, format.getSampleRate());
		}

		// Spread/downmix channels now if not done formerly
		if (! matchChannelCount(format, channels.size()))
		{
			if (channels == channelList)
				channels = copy(channelList, sampleCount);
			
			convertChannelCount(channels, format.getChannels());
		}
		
		int bytesPerSample = format.getSampleSizeInBits() / 8;
		int bytesPerFrame = bytesPerSample * format.getChannels();
		SampleType formatType = SampleType.get(format);
		int off = 0;
		
		// Convert sample data to bytes
		for (int ch = 0; ch < format.getChannels(); ch++, off += bytesPerSample)
			convertFloatToByte(
				channels.get(ch),
				numSamples,
				data,
				off,
				bytesPerFrame,
				formatType
			);
		
		return byteSize;
	}
	
	public int getBytes(byte[] data, AudioFormat format)
	{
		return getBytes(data, format, sampleCount);
	}
	
	/**
	 * Creates a precisely-sized byte array and writes sample data from this
	 * buffer into it, considering format.
	 */
	public byte[] getBytes(AudioFormat format)
	{
		byte[] data = new byte[getByteSize(format)];
		getBytes(data, format);
		return data;
	}
	
	/*------------------------------------------------------------------------------------------[*]
	 * Channel actions: add, remove, copy, mix.
	 */
	
	public void makeSilence()
	{
		for (int ch = 0; ch < channelList.size(); ch++)
			Arrays.fill(channelList.get(ch), 0.0f);
	}
	
	public void makeSilence(int channel)
	{
		Arrays.fill(channelList.get(channel), 0.0f);
	}
	
	public void scale(float factor)
	{
		for (int ch = 0; ch < channelList.size(); ch++)
			scale(factor, ch);
	}
	
	public void scale(float factor, int channel)
	{
		if (factor == 1.0)
			return;
		
		float[] samples = channelList.get(channel);
		
		for (int i = 0; i < sampleCount; i++)
			samples[i] *= factor;
	}
	
	/**
	 * Returns the index of the new channel.
	 */
	public int addChannel()
	{
		int index = getChannelCount();
		insertChannel(index);
		return index;
	}
	
	public void insertChannel(int index)
	{
		channelList.add(index, new float[length()]);
	}
	
	public void removeChannel(int index)
	{
		channelList.remove(index);
	}
	
	public void copyChannel(int from, int to)
	{
		System.arraycopy(getChannel(from), 0, getChannel(to), 0, length());
	}
	
	public void swapChannels(int from, int to)
	{
		float[] temp = channelList.get(from);
		channelList.set(from, channelList.get(to));
		channelList.set(to, temp);
	}
	
	/**
	 * Only works for 2-channel buffers.
	 * 
	 * @throws IllegalStateException If buffer doesn't have exactly 2 channels.
	 */
	public void swapChannels()
	{
		if (channelList.size() != 2)
			throw new IllegalStateException("Must be stereo");
		
		swapChannels(0, 1);
	}
	
	public void mixChannels(int[] from, int to)
	{
		for (int i = 0; i < sampleCount; ++i)
		{
			float sample = 0;
			
			for (int c = 0; c < from.length; ++c)
				sample += channelList.get(from[c])[i];
			
			sample /= from.length;
			channelList.get(to)[i] = sample;
		}
	}
	
	public void mixToMono()
	{
		int channelCount = channelList.size();
		
		if (channelCount == 0 || channelCount == 1)
			return;
		
		int[] sourceChannels = new int[channelCount];
		
		for (int c = 0; c < sourceChannels.length; ++c)
			sourceChannels[c] = c;
		
		mixChannels(sourceChannels, 0);
		
		while (getChannelCount() > 1)
			removeChannel(1);
	}
	
	/**
	 * Only works for 1-channel buffers.
	 * 
	 * @throws IllegalStateException If buffer doesn't have exactly 1 channel.
	 */
	public void spreadToStereo()
	{
		if (channelList.size() != 1)
			throw new IllegalStateException("Must be mono");
		
		addChannel();
		copyChannel(0, 1);
	}
	
	/*------------------------------------------------------------------------------------------[*]
	 * Conversion methods.
	 */
	
	/**
	 * Changes buffer length to given size. Only if size is increased beyond
	 * current capacity will underlying arrays be resized and replaced.
	 * Any float[] from the getChannel() method will no longer share memory
	 * with this buffer in that case.
	 */
	public void resize(int size)
	{
		if (size < 0)
			throw new IllegalArgumentException(String.valueOf(size));
		
		if (size > sampleCount)
		{
			for (int ch = 0; ch < channelList.size(); ++ch)
			{
				float[] newChannel = new float[size];
				System.arraycopy(getChannel(ch), 0, newChannel, 0, sampleCount);
				channelList.set(ch, newChannel);
			}
		}
		
		sampleCount = size;
	}
	
	/**
	 * Only supports mono->stereo and stereo->mono.
	 */
	public void rechannel(int channels)
	{
		if (channels == channelList.size())
			return;
		
		convertChannelCount(channelList, channels);
	}
	
	/**
	 * Makes no changes to this buffer.
	 * Uses current sampleCount of this buffer.
	 */
	private void convertChannelCount(List<float[]> channels, int newCount)
	{
		int oldCount = channels.size();
		
		if (oldCount == 1 && newCount == 2)
		{
			float[] left  = channels.get(0);
			float[] right = Arrays.copyOf(left, left.length);
			channels.add(1, right);
		}
		else if (oldCount == 2 && newCount == 1)
		{
			float[] left  = channels.get(0);
			float[] right = channels.get(1);
			
			for (int i = 0; i < sampleCount; ++i)
			{
				left[i] += right[i];
				left[i] /= 2;
			}
			
			channels.remove(1);
		}
		else
		{
			StringBuilder message = new StringBuilder();
			message.append("cannot convert channels ");
			message.append(channelList.size());
			message.append(" -> ");
			message.append(channels);
			throw new UnsupportedOperationException(message.toString());
		}
	}
	
	/**
	 * Performs sample rate conversion on this buffer. The length of samples
	 * and the sample rate will both be changed when this method returns.
	 * New float arrays will not share memory space with arrays previously
	 * returned by getChannel().
	 */
	public void resample(float rate)
	{
		if (rate == sampleRate)
			return;
		
		sampleCount = convertSampleRate(channelList, rate);
		sampleRate = rate;
	}
	
	/**
	 * Makes no changes to this buffer.
	 * Uses current sampleCount/Length of this buffer.
	 * Returns new sample length.
	 */
	private int convertSampleRate(List<float[]> channels, float rate)
	{
		float rateRatio = rate / sampleRate;
		int oldLength = sampleCount;
		int newLength = (int) (sampleCount * rateRatio);
		
		for (int ch = 0; ch < channels.size(); ++ch)
		{
			float[] newChannel = new float[newLength];
			float[] oldChannel = channels.get(ch);
			
			if (rateRatio > 1) // Linear Interpolation
			{
				int previousIndex = 0;
				
				for (int i = 0; i < oldLength; ++i)
				{
					int i2 = index(i, newLength, rateRatio);
					newChannel[i2] = oldChannel[i];
					
					// Interpolate over spaced region
					if (i2 - previousIndex > 1)
					{
						float space = i2 - previousIndex;
						float base = newChannel[previousIndex];
						float diff = newChannel[i2] - base;
						
						for (int y = 1, x = previousIndex + 1; x < i2; ++x, ++y)
							newChannel[x] = base + (y / space) * diff;
					}
					
					previousIndex = i2;
				}
			}
			else // Decimation
			{
				rateRatio = 1 / rateRatio;
				
				for (int i = 0; i < newLength; ++i)
				{
					int i2 = index(i, oldLength, rateRatio);
					newChannel[i] = oldChannel[i2];
				}
			}
			
			channels.set(ch, newChannel);
		}
		
		return newLength;
	}
	
	private static int index(int i, int size, float ratio)
	{
		return Math.max(Math.min((int) (i * ratio), size - 1), 0);
	}
	
	/*------------------------------------------------------------------------------------------[*]
	 * Property setters/getters.
	 */
	
	public boolean formatMatches(SampleBuffer buffer)
	{
		return sampleRate == buffer.sampleRate
			&& channelList.size() == buffer.channelList.size();
	}
	
	public int getChannelCount()
	{
		return channelList.size();
	}
	
	public float getSampleRate()
	{
		return sampleRate;
	}
	
	public int length()
	{
		return sampleCount;
	}
	
	/**
	 * Returned array shares memory space with buffer. Changing values in array
	 * changes sample values in buffer.
	 */
	public float[] getChannel(int index)
	{
		return channelList.get(index);
	}
	
	/**
	 * Returned arrays share memory space with buffer. Changing values in arrays
	 * changes sample values in buffer.
	 */
	public List<float[]> getAllChannels()
	{
		return Arrays.asList(channelList.toArray(new float[0][]));
	}
	
	/**
	 * A value between 0.2 and 0.9 gives best results.
	 */
	public void setDitherBits(float ditherBits)
	{
		if (ditherBits <= 0)
			throw new IllegalArgumentException("DitherBits must be greater than 0");
		
		this.ditherBits=ditherBits;
	}
	
	public float getDitherBits()
	{
		return ditherBits;
	}
	
	public void setDitherMode(DitherMode mode)
	{
		this.ditherMode = mode;
	}
	
	public DitherMode getDitherMode()
	{
		return ditherMode;
	}
	
	/*------------------------------------------------------------------------------------------[*]
	 * Low-level float<-->byte[] block conversion.
	 */
	
	private static void convertByteToFloat(
		byte[] input,
		int offset,
		int sampleCount,
		float[] output,
		int bytesPerFrame,
		SampleType type)
	{
		for (int i = 0; i < sampleCount; i++, offset += bytesPerFrame)
		{
			switch (type) {
			case SIGNED_8BIT:
				output[i]=
				    ((float) input[offset])*invTwoPower7;
				break;
			case UNSIGNED_8BIT:
				output[i]=
				    ((float) ((input[offset] & 0xFF)-128))*invTwoPower7;
				break;
			case SIGNED_16BIT_BIG_ENDIAN:
				output[i]=
				    ((float) ((input[offset]<<8)
				              | (input[offset+1] & 0xFF)))*invTwoPower15;
				break;
			case SIGNED_16BIT_LITTLE_ENDIAN:
				output[i]=
				    ((float) ((input[offset+1]<<8)
				              | (input[offset] & 0xFF)))*invTwoPower15;
				break;
			case SIGNED_24BIT_BIG_ENDIAN:
				output[i]=
				    ((float) ((input[offset]<<16)
				              | ((input[offset+1] & 0xFF)<<8)
				              | (input[offset+2] & 0xFF)))*invTwoPower23;
				break;
			case SIGNED_24BIT_LITTLE_ENDIAN:
				output[i]=
				    ((float) ((input[offset+2]<<16)
				              | ((input[offset+1] & 0xFF)<<8)
				              | (input[offset] & 0xFF)))*invTwoPower23;
				break;
			case SIGNED_32BIT_BIG_ENDIAN:
				output[i]=
				    ((float) ((input[offset]<<24)
				              | ((input[offset+1] & 0xFF)<<16)
				              | ((input[offset+2] & 0xFF)<<8)
				              | (input[offset+3] & 0xFF)))*invTwoPower31;
				break;
			case SIGNED_32BIT_LITTLE_ENDIAN:
				output[i]=
				    ((float) ((input[offset+3]<<24)
				              | ((input[offset+2] & 0xFF)<<16)
				              | ((input[offset+1] & 0xFF)<<8)
				              | (input[offset] & 0xFF)))*invTwoPower31;
				break;
			default:
				throw new IllegalArgumentException("Unsupported: " + type);
			}
		}
	}
	
	private void convertFloatToByte(
		float[] input,
		int sampleCount,
		byte[] output,
		int off,
		int bytesPerFrame,
		SampleType type)
	{
		boolean dither = false;
		boolean sampleSizeShrunk =
			originalFormatType != null &&
			originalFormatType.sampleSizeInBits > type.sampleSizeInBits;
		
		switch (ditherMode)
		{
		case AUTO: dither = sampleSizeShrunk; break;
		case ON:   dither = true;  break;
		case OFF:  dither = false; break;
		}
		
		int word;
		
		for (int i = 0; i < sampleCount; i++, off += bytesPerFrame)
		{
			switch (type)
			{
			case SIGNED_8BIT:
				output[off] = to8(input[i], dither);
				break;
			case UNSIGNED_8BIT:
				output[off] = (byte) (to8(input[i], dither) + 128);
				break;
			case SIGNED_16BIT_BIG_ENDIAN:
				word = to16(input[i], dither);
				output[off + 0] = (byte) (word >> 8);
				output[off + 1] = (byte) (word & 0xff);
				break;
			case SIGNED_16BIT_LITTLE_ENDIAN:
				word = to16(input[i], dither);
				output[off + 1] = (byte) (word >> 8);
				output[off + 0] = (byte) (word & 0xff);
				break;
			case SIGNED_24BIT_BIG_ENDIAN:
				word = to24(input[i], dither);
				output[off + 0] = (byte) (word >> 16);
				output[off + 1] = (byte) ((word >>> 8) & 0xff);
				output[off + 2] = (byte) (word & 0xff);
				break;
			case SIGNED_24BIT_LITTLE_ENDIAN:
				word = to24(input[i], dither);
				output[off + 2] = (byte) (word >> 16);
				output[off + 1] = (byte) ((word >>> 8) & 0xff);
				output[off + 0] = (byte) (word & 0xff);
				break;
			case SIGNED_32BIT_BIG_ENDIAN:
				word = to32(input[i], dither);
				output[off + 0] = (byte) (word >> 24);
				output[off + 1] = (byte) ((word >>> 16) & 0xff);
				output[off + 2] = (byte) ((word >>> 8) & 0xff);
				output[off + 3] = (byte) (word & 0xff);
				break;
			case SIGNED_32BIT_LITTLE_ENDIAN:
				word = to32(input[i], dither);
				output[off + 3] = (byte) (word >> 24);
				output[off + 2] = (byte) ((word >>> 16) & 0xff);
				output[off + 1] = (byte) ((word >>> 8) & 0xff);
				output[off + 0] = (byte) (word & 0xff);
				break;
			default:
				throw new IllegalArgumentException("Unsupported: " + type);
			}
		}
	}
	
	private byte to8(float sample, boolean dither)
	{
		return (byte) to(sample, twoPower7, -128.0f, 127.0f, dither);
	}
	
	private int to16(float sample, boolean dither)
	{
		return to(sample, twoPower15, -32768.0f, 32767.0f, dither);
	}
	
	private int to24(float sample, boolean dither)
	{
		return to(sample, twoPower23, -8388608.0f, 8388607.0f, dither);
	}
	
	private int to32(float sample, boolean dither)
	{
		return to(sample, twoPower31, -2147483648.0f, 2147483647.0f, dither);
	}
	
	private int to(float sample, float factor, float min, float max, boolean dither)
	{
		sample *= factor;
		
		if (dither)
			sample += Math.random() * ditherBits;
		
		if      (sample >= max) return (int) max;
		else if (sample <= min) return (int) min;
		else if (sample < 0)    return (int) (sample - 0.5f);
		else                    return (int) (sample + 0.5f);
	}
	
	private static final float twoPower7  = 128.0f;
	private static final float twoPower15 = 32768.0f;
	private static final float twoPower23 = 8388608.0f;
	private static final float twoPower31 = 2147483648.0f;
	
	private static final float invTwoPower7  = 1 / twoPower7;
	private static final float invTwoPower15 = 1 / twoPower15;
	private static final float invTwoPower23 = 1 / twoPower23;
	private static final float invTwoPower31 = 1 / twoPower31;
	
	private static enum SampleType
	{
		SIGNED_8BIT               (8,  true,  false),
		UNSIGNED_8BIT             (8,  false, false),
		SIGNED_16BIT_BIG_ENDIAN   (16, true,  true),
		SIGNED_16BIT_LITTLE_ENDIAN(16, true,  false),
		SIGNED_24BIT_BIG_ENDIAN   (24, true,  true),
		SIGNED_24BIT_LITTLE_ENDIAN(24, true,  false),
		SIGNED_32BIT_BIG_ENDIAN   (32, true,  true),
		SIGNED_32BIT_LITTLE_ENDIAN(32, true,  false);
		
		public int sampleSizeInBits;
		public boolean signed;
		public boolean bigEndian;
		
		private SampleType(int ssib, boolean s, boolean be)
		{
			sampleSizeInBits = ssib;
			signed = s;
			bigEndian = be;
			
			if (!(ssib == 8 || ssib == 16 || ssib == 24 || ssib == 32))
				throw new IllegalArgumentException("Unsupported sample size");
			if (ssib != 8 && !s)
				throw new IllegalArgumentException("Unsupported sampleSize/signed combo");
		}
		
		public static SampleType get(AudioFormat format)
		{
			int ssib = format.getSampleSizeInBits();
			boolean s = isSigned(format);
			boolean be = format.isBigEndian();
			
			for (SampleType bFormat : values())
			{
				if (ssib == bFormat.sampleSizeInBits
				 &&    s == bFormat.signed
				 &&  (be == bFormat.bigEndian || ssib == 8))
					return bFormat;
			}
			
			throw new IllegalArgumentException("Undefined sample format");
		}
	}
	
	private static boolean isPCM(AudioFormat format)
	{
		return format.getEncoding() == Encoding.PCM_SIGNED
			|| format.getEncoding() == Encoding.PCM_UNSIGNED;
	}
	
	private static boolean isSigned(AudioFormat format)
	{
		return format.getEncoding().equals(Encoding.PCM_SIGNED);
	}
	
	private static boolean isFullySpecified(AudioFormat format)
	{
		return format.getSampleSizeInBits() > 0
			&& format.getChannels() > 0
			&& format.getSampleRate() > 0;
	}
	
	private static boolean matchSampleRate(AudioFormat format, float rate)
	{
		return format.getSampleRate() == rate
			|| format.getSampleRate() == NS;
	}
	
	private static boolean matchChannelCount(AudioFormat format, int channels)
	{
		return format.getChannels() == channels
			|| format.getChannels() == NS;
	}
	
	private static int round(int value, int multiple)
	{
		return value - (value % multiple);
	}
}
