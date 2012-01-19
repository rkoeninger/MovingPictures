namespace MPSharp
{
    partial class MainWindow
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
			this.menuStrip1 = new System.Windows.Forms.MenuStrip();
			this.engineToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.pauseToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.exitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.displayToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.showGridToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.showSurfaceToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.unitToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.addToolStripMenuItem = new System.Windows.Forms.ToolStripMenuItem();
			this.statusStrip1 = new System.Windows.Forms.StatusStrip();
			this.toolStrip1 = new System.Windows.Forms.ToolStrip();
			this.panel1 = new System.Windows.Forms.Panel();
			this.menuStrip1.SuspendLayout();
			this.SuspendLayout();
			// 
			// menuStrip1
			// 
			this.menuStrip1.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.engineToolStripMenuItem,
            this.displayToolStripMenuItem,
            this.unitToolStripMenuItem});
			this.menuStrip1.Location = new System.Drawing.Point(0, 0);
			this.menuStrip1.Name = "menuStrip1";
			this.menuStrip1.Size = new System.Drawing.Size(512, 24);
			this.menuStrip1.TabIndex = 0;
			this.menuStrip1.Text = "menuStrip1";
			// 
			// engineToolStripMenuItem
			// 
			this.engineToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.pauseToolStripMenuItem,
            this.exitToolStripMenuItem});
			this.engineToolStripMenuItem.Name = "engineToolStripMenuItem";
			this.engineToolStripMenuItem.Size = new System.Drawing.Size(55, 20);
			this.engineToolStripMenuItem.Text = "Engine";
			// 
			// pauseToolStripMenuItem
			// 
			this.pauseToolStripMenuItem.Name = "pauseToolStripMenuItem";
			this.pauseToolStripMenuItem.Size = new System.Drawing.Size(105, 22);
			this.pauseToolStripMenuItem.Text = "Pause";
			// 
			// exitToolStripMenuItem
			// 
			this.exitToolStripMenuItem.Name = "exitToolStripMenuItem";
			this.exitToolStripMenuItem.Size = new System.Drawing.Size(105, 22);
			this.exitToolStripMenuItem.Text = "Exit";
			this.exitToolStripMenuItem.Click += new System.EventHandler(this.exitToolStripMenuItem_Click);
			// 
			// displayToolStripMenuItem
			// 
			this.displayToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.showGridToolStripMenuItem,
            this.showSurfaceToolStripMenuItem});
			this.displayToolStripMenuItem.Name = "displayToolStripMenuItem";
			this.displayToolStripMenuItem.Size = new System.Drawing.Size(57, 20);
			this.displayToolStripMenuItem.Text = "Display";
			// 
			// showGridToolStripMenuItem
			// 
			this.showGridToolStripMenuItem.CheckOnClick = true;
			this.showGridToolStripMenuItem.Name = "showGridToolStripMenuItem";
			this.showGridToolStripMenuItem.Size = new System.Drawing.Size(145, 22);
			this.showGridToolStripMenuItem.Text = "Show Grid";
			this.showGridToolStripMenuItem.Click += new System.EventHandler(this.showGridToolStripMenuItem_Click);
			// 
			// showSurfaceToolStripMenuItem
			// 
			this.showSurfaceToolStripMenuItem.CheckOnClick = true;
			this.showSurfaceToolStripMenuItem.Name = "showSurfaceToolStripMenuItem";
			this.showSurfaceToolStripMenuItem.Size = new System.Drawing.Size(145, 22);
			this.showSurfaceToolStripMenuItem.Text = "Show Surface";
			this.showSurfaceToolStripMenuItem.Click += new System.EventHandler(this.showSurfaceToolStripMenuItem_Click);
			// 
			// unitToolStripMenuItem
			// 
			this.unitToolStripMenuItem.DropDownItems.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.addToolStripMenuItem});
			this.unitToolStripMenuItem.Name = "unitToolStripMenuItem";
			this.unitToolStripMenuItem.Size = new System.Drawing.Size(46, 20);
			this.unitToolStripMenuItem.Text = "Units";
			// 
			// addToolStripMenuItem
			// 
			this.addToolStripMenuItem.Name = "addToolStripMenuItem";
			this.addToolStripMenuItem.Size = new System.Drawing.Size(96, 22);
			this.addToolStripMenuItem.Text = "Add";
			// 
			// statusStrip1
			// 
			this.statusStrip1.Location = new System.Drawing.Point(0, 561);
			this.statusStrip1.Name = "statusStrip1";
			this.statusStrip1.Size = new System.Drawing.Size(512, 22);
			this.statusStrip1.TabIndex = 1;
			this.statusStrip1.Text = "statusStrip1";
			// 
			// toolStrip1
			// 
			this.toolStrip1.Location = new System.Drawing.Point(0, 24);
			this.toolStrip1.Name = "toolStrip1";
			this.toolStrip1.Size = new System.Drawing.Size(512, 25);
			this.toolStrip1.TabIndex = 2;
			this.toolStrip1.Text = "toolStrip1";
			// 
			// panel1
			// 
			this.panel1.Location = new System.Drawing.Point(0, 49);
			this.panel1.Margin = new System.Windows.Forms.Padding(0);
			this.panel1.Name = "panel1";
			this.panel1.Size = new System.Drawing.Size(512, 512);
			this.panel1.TabIndex = 3;
			this.panel1.Paint += new System.Windows.Forms.PaintEventHandler(this.panel1_Paint);
			// 
			// MainWindow
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(512, 583);
			this.Controls.Add(this.panel1);
			this.Controls.Add(this.toolStrip1);
			this.Controls.Add(this.statusStrip1);
			this.Controls.Add(this.menuStrip1);
			this.DoubleBuffered = true;
			this.MainMenuStrip = this.menuStrip1;
			this.Name = "MainWindow";
			this.Text = "MPSharp";
			this.menuStrip1.ResumeLayout(false);
			this.menuStrip1.PerformLayout();
			this.ResumeLayout(false);
			this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.MenuStrip menuStrip1;
        private System.Windows.Forms.ToolStripMenuItem engineToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem displayToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem unitToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem pauseToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem exitToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem showGridToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem showSurfaceToolStripMenuItem;
        private System.Windows.Forms.ToolStripMenuItem addToolStripMenuItem;
        private System.Windows.Forms.StatusStrip statusStrip1;
        private System.Windows.Forms.ToolStrip toolStrip1;
        private System.Windows.Forms.Panel panel1;
    }
}

