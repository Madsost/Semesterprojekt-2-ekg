package main.util;

/**
 * <h1>Hjælpeklasse til at filtrere data.</h1> </br>
 * 
 * Implementerer et båndpasfilter i java.</br>
 * Specifikationer: båndpas 0.25-->40Hz </br>
 * 200. orden FIR filter med vinduesfunktion </br>
 * vinduestype: taylor</br>
 * 
 * @author Mads Østergaard
 *
 */
public class Filter {
	/**
	 * Koefficienterne til et båndpasfilter.
	 */
	private static double[] coeffs = { -0.000456, -0.001121, -0.001181, -0.000565, 0.000167, 0.000327, -0.000265,
			-0.001087, -0.001379, -0.000842, 0.000049, 0.000459, -0.000044, -0.001047, -0.001634, -0.001226, -0.000157,
			0.000581, 0.000239, -0.000950, -0.001920, -0.001735, -0.000500, 0.000661, 0.000601, -0.000738, -0.002185,
			-0.002369, -0.001028, 0.000645, 0.001039, -0.000353, -0.002351, -0.003094, -0.001783, 0.000458, 0.001516,
			0.000250, -0.002326, -0.003849, -0.002786, 0.000018, 0.001968, 0.001091, -0.002012, -0.004542, -0.004034,
			-0.000765, 0.002300, 0.002165, -0.001312, -0.005055, -0.005496, -0.001976, 0.002392, 0.003437, -0.000129,
			-0.005245, -0.007117, -0.003704, 0.002095, 0.004849, 0.001636, -0.004940, -0.008818, -0.006050, 0.001220,
			0.006320, 0.004112, -0.003912, -0.010507, -0.009173, -0.000501, 0.007757, 0.007524, -0.001810, -0.012082,
			-0.013395, -0.003538, 0.009062, 0.012377, 0.002057, -0.013440, -0.019540, -0.008979, 0.010142, 0.020125,
			0.009569, -0.014488, -0.030384, -0.020444, 0.010912, 0.036818, 0.028987, -0.015150, -0.062475, -0.063333,
			0.011314, 0.142314, 0.267459, 0.318885, 0.267459, 0.142314, 0.011314, -0.063333, -0.062475, -0.015150,
			0.028987, 0.036818, 0.010912, -0.020444, -0.030384, -0.014488, 0.009569, 0.020125, 0.010142, -0.008979,
			-0.019540, -0.013440, 0.002057, 0.012377, 0.009062, -0.003538, -0.013395, -0.012082, -0.001810, 0.007524,
			0.007757, -0.000501, -0.009173, -0.010507, -0.003912, 0.004112, 0.006320, 0.001220, -0.006050, -0.008818,
			-0.004940, 0.001636, 0.004849, 0.002095, -0.003704, -0.007117, -0.005245, -0.000129, 0.003437, 0.002392,
			-0.001976, -0.005496, -0.005055, -0.001312, 0.002165, 0.002300, -0.000765, -0.004034, -0.004542, -0.002012,
			0.001091, 0.001968, 0.000018, -0.002786, -0.003849, -0.002326, 0.000250, 0.001516, 0.000458, -0.001783,
			-0.003094, -0.002351, -0.000353, 0.001039, 0.000645, -0.001028, -0.002369, -0.002185, -0.000738, 0.000601,
			0.000661, -0.000500, -0.001735, -0.001920, -0.000950, 0.000239, 0.000581, -0.000157, -0.001226, -0.001634,
			-0.001047, -0.000044, 0.000459, 0.000049, -0.000842, -0.001379, -0.001087, -0.000265, 0.000327, 0.000167,
			-0.000565, -0.001181, -0.001121, -0.000456 };
	private static int length = coeffs.length;
	private static double[] delayLine = new double[length];
	private static int count;

	/**
	 * Savitzky–Golay udjævner-filter
	 */
	private static double[] smoothCoeffs = { -2, 3, 6, 7, 6, 3, -2 };
	private static int smoothLength = smoothCoeffs.length;
	private static double[] smoothDelayLine = new double[smoothLength];
	private static int smoothCount;
	private static double normalize = 1.0 / 21.0;

	/**
	 * S
	 * 
	 * @param data
	 * @return
	 */
	public static double doFilter(double data) {
		// indsæt input på næste plads
		delayLine[count] = data;
		double result = 0.0;

		// gennemløb listerne og fold delayLine med coeffs: Sum(x(n-k)*h(n))
		int index = count;
		for (int i = 0; i < length; i++) {
			result += coeffs[i] * delayLine[index];
			// tæl index ned for at få den omvendte sekvens
			index--;

			// hvis index er < 0 skal vi fortsætte i max
			if (index < 0)
				index = length - 1;
		}
		// tæl count op, så næste måling kommer ind på næste plads
		count++;

		// hvis count er større end længden, sættes ind på plads 0.
		if (count >= length)
			count = 0;
		return result;
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static double doSmooth(double input) {
		// indsæt input på næste plads
		smoothDelayLine[smoothCount] = input;
		double result = 0.0;

		// gennemløb listerne og fold delayLine med coeffs: Sum(x(n-k)*h(n))
		int index = smoothCount;
		for (int i = 0; i < smoothLength; i++) {
			result += smoothCoeffs[i] * smoothDelayLine[index]; // * (1 / 231);
			// tæl index ned for at få den omvendte sekvens
			index--;

			// hvis index er < 0 skal vi fortsætte i max
			if (index < 0)
				index = smoothLength - 1;
		}
		// tæl count op, så næste måling kommer ind på næste plads
		smoothCount++;

		// hvis count er større end længden, sættes ind på plads 0.
		if (smoothCount >= smoothLength)
			smoothCount = 0;
		// normalisering ( 1/21 ):
		result *= normalize;
		return result;
	}

}
