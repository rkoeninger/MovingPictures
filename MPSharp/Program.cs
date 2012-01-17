using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;

using MPSharp.Basics;

namespace MPSharp
{
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
			foreach (GridPos dir in new GridPos(3, 4).Get8Neighbors(Direction.NW)) Console.WriteLine(dir);

            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainWindow());
        }
    }
}
