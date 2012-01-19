using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Windows.Forms;

using MPSharp.Basics;
using MPSharp.Map;

namespace MPSharp
{
    public partial class MainWindow : Form
    {
		TileSet tileSet;
		MapGrid map;
		bool showGrid;
		bool showSurface;

        public MainWindow()
        {
            InitializeComponent();

			String cd = Environment.CurrentDirectory;

			tileSet = TileSet.Load("..\\..\\..\\MovingPictures\\res\\tileset\\newTerraDirt");
			map = new MapGrid(16, 16);

			Random rand = new Random();

			foreach (GridPos pos in map.Bounds)
				map.SetTileCode(pos, "plain" + (rand.Next() % 8));
        }

        private void exitToolStripMenuItem_Click(object sender, EventArgs e)
        {
            Environment.Exit(0);
        }

        private void panel1_Paint(object sender, PaintEventArgs e)
        {
            Graphics g = e.Graphics;
			g.Clear(Color.Red);
			
			if (showSurface)
			{
				foreach (GridPos pos in map.Bounds)
					g.DrawImage(tileSet[map.GetTileCode(pos)].Bitmap, new Point(pos.X * 32, pos.Y * 32));
			}

			if (showGrid)
			{
				Pen p = new Pen(Color.Blue);

				for (int u = 1; u < 16; ++u)
					g.DrawLine(p, u * 32, 0, u * 32, panel1.Height);
				
				for (int v = 1; v < 16; ++v)
					g.DrawLine(p, 0, v * 32, panel1.Width, v * 32);
			}
        }

		private void showGridToolStripMenuItem_Click(object sender, EventArgs e)
		{
			showGrid = showGridToolStripMenuItem.Checked;
			Refresh();
		}

		private void showSurfaceToolStripMenuItem_Click(object sender, EventArgs e)
		{
			showSurface = showSurfaceToolStripMenuItem.Checked;
			Refresh();
		}
    }
}
