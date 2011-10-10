package com.robbix.mp5.basics.sound;

import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class Encodings extends AudioFormat.Encoding
{
	/** contains all known encodings */
	private static StringHashedSet encodings = new StringHashedSet();

	// initially add the standard encodings
	static {
		encodings.add(AudioFormat.Encoding.PCM_SIGNED);
		encodings.add(AudioFormat.Encoding.PCM_UNSIGNED);
		encodings.add(AudioFormat.Encoding.ULAW);
		encodings.add(AudioFormat.Encoding.ALAW);
	}

	Encodings(String name) {
		super(name);
	}

	/**
	 * Use this method for retrieving an instance of
	 * <code>AudioFormat.Encoding</code> of the specified
	 * name. A standard registry of encoding names will
	 * be maintained by the Tritonus team.
	 * <p>
	 * Every file reader, file writer, and format converter
	 * provider should exclusively use this method for
	 * retrieving instances of <code>AudioFormat.Encoding</code>.
	 */
	public static AudioFormat.Encoding getEncoding(String name) {
		AudioFormat.Encoding res=(AudioFormat.Encoding) encodings.get(name);
		if (res==null) {
			// it is not already in the string set. Create a new encoding instance.
			res=new Encodings(name);
			// and save it for the future
			encodings.add(res);
		}
		return res;
	}

	/**
	 * Returns all &quot;supported&quot; encodings. 
	 * Supported means that it is possible to read or
	 * write files with this encoding, or that a converter
	 * accepts this encoding as source or target format.
	 * <p>
	 * Currently, this method returns a best guess and
	 * the search algorithm is far from complete: with standard
	 * methods of AudioSystem, only the target encodings
	 * of the converters can be retrieved - neither
	 * the source encodings of converters nor the encodings
	 * of file readers and file writers cannot be retrieved.
	 */
	public static AudioFormat.Encoding[] getEncodings() {
		StringHashedSet iteratedSources=new StringHashedSet();
		StringHashedSet retrievedTargets=new StringHashedSet();
		Iterator<Object> sourceFormats=encodings.iterator();
		while (sourceFormats.hasNext()) {
			AudioFormat.Encoding source=(AudioFormat.Encoding) sourceFormats.next();
			iterateEncodings(source, iteratedSources, retrievedTargets);
		}
		return (AudioFormat.Encoding[]) retrievedTargets.toArray(
		           new AudioFormat.Encoding[retrievedTargets.size()]);
	}


	private static void iterateEncodings(AudioFormat.Encoding source,
	                                     StringHashedSet iteratedSources,
	                                     StringHashedSet retrievedTargets) {
		if (!iteratedSources.contains(source)) {
			iteratedSources.add(source);
			AudioFormat.Encoding[] targets=AudioSystem.getTargetEncodings(source);
			for (int i=0; i<targets.length; i++) {
				AudioFormat.Encoding target=targets[i];
				if (retrievedTargets.add(target.toString())) {
					iterateEncodings(target, iteratedSources,retrievedTargets);
				}
			}
		}
	}
}
