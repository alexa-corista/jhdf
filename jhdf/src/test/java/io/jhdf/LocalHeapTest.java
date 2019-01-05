package io.jhdf;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.jhdf.LocalHeap;
import io.jhdf.Superblock;
import io.jhdf.Utils;

public class LocalHeapTest {
	private FileChannel fc;
	private RandomAccessFile raf;
	private Superblock sb;

	@Before
	public void setUp() throws FileNotFoundException {
		final String testFileUrl = this.getClass().getResource("test_file.hdf5").getFile();
		raf = new RandomAccessFile(new File(testFileUrl), "r");
		fc = raf.getChannel();
		sb = Superblock.readSuperblock(fc, 0);
	}

	@After
	public void after() throws IOException {
		raf.close();
		fc.close();
	}

	@Test
	public void testLocalHeap() throws IOException {
		LocalHeap heap = new LocalHeap(fc, 680, sb);

		assertThat(heap.getVersion(), is(equalTo((short) 0)));
		assertThat(heap.getDataSegmentSize(), is(equalTo(88L)));
		assertThat(heap.getOffsetToHeadOfFreeList(), is(equalTo(24L)));
		assertThat(heap.getAddressOfDataSegment(), is(equalTo(712L)));
		assertThat(heap.toString(), is(equalTo(
				"LocalHeap [address=0x2a8, version=0, dataSegmentSize=88, offsetToHeadOfFreeList=24, addressOfDataSegment=0x2c8]")));
	}

	@Test
	public void testAccessingData() throws Exception {
		LocalHeap heap = new LocalHeap(fc, 680, sb);
		ByteBuffer bb = heap.getDataBuffer();
		assertThat(bb.capacity(), is(equalTo(88)));
		// Test reading a name from the heap
		bb.position(8);
		assertThat(Utils.readUntilNull(bb), is("datasets_group"));
	}
}