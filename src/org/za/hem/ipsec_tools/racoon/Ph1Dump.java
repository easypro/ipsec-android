package org.za.hem.ipsec_tools.racoon;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.AbstractCollection;
import java.util.ArrayList;

import android.util.Log;

// FIXME change to list/array of dumps.
public class Ph1Dump extends Command {
	
	private ArrayList<Item> mItems;
	
	public class Item {
		int mStatus;
		int mSide;
		InetSocketAddress mRemote;
		InetSocketAddress mLocal;
		short mVersion;
		short mEType;
		long mTimeCreated;
		int mPh2Cnt;

		public Item(
				// index
				int status, int side,
				InetSocketAddress remote,
				InetSocketAddress local,
				short version,
				short eType,
				long timeCreated, // TODO change to time?
				int ph2Cnt) {
			mStatus = status;
			mSide = side;
			mRemote = remote;
			mLocal = local;
			mVersion = version;
			mEType = eType;
			mTimeCreated = timeCreated;
			mPh2Cnt = ph2Cnt;
		}
	}
	
	protected Ph1Dump(int len) {
		super(ADMIN_SHOW_SA, ADMIN_PROTO_ISAKMP, len);
		mItems = new ArrayList<Item>();
	}
	
	public AbstractCollection<Item> getItems() { return mItems; }
	
	private static final String HEXES = "0123456789ABCDEF";
	private static String getHex( byte [] raw ) {
	    if ( raw == null ) {
	      return null;
	    }
	    final StringBuilder hex = new StringBuilder( 2 * raw.length );
	    for ( final byte b : raw ) {
	      hex.append(HEXES.charAt((b & 0xF0) >> 4))
	         .append(HEXES.charAt((b & 0x0F)));
	    }
	    return hex.toString();
	}
	
	protected static Ph1Dump create(int len, byte[] data) {
		ByteBuffer bb = wrap(data);
		Ph1Dump pd = new Ph1Dump(len);
		
		int count = len / (8 + 8 + 4 + 4 + 128 + 128 + 2 + 2 + 4 + 4);
		
		Log.i("ipsec-tools", "Ph1Dump create len:" + len + " count:" + count);
		
		for (int i = 0; i<count; i++) {
			Log.i("ipsec-tools", "pos:" + bb.position());

			// FIXME use cookie
			byte[] cookie = new byte[16];
			// TODO read i_ck u_char cookie_t[8]
			bb.get(cookie, 0, 8);
			// TODO read r_ck u_char cookie_t[8]
			bb.get(cookie, 8, 8);
			Log.i("ipsec-tools", "cookie:" + getHex(cookie));
			int status = bb.getInt();
			Log.i("ipsec-tools", "status:" + status);
			int side = bb.getInt();
			Log.i("ipsec-tools", "side:" + side);
			InetSocketAddress remote = getSocketAddressStorage(bb);
			Log.i("ipsec-tools", "remote:" + remote);
			InetSocketAddress local = getSocketAddressStorage(bb);
			Log.i("ipsec-tools", "local:" + local);
			short version = getUnsignedByte(bb);
			Log.i("ipsec-tools", "version:" + version);
			short eType = getUnsignedByte(bb);
			Log.i("ipsec-tools", "eType:" + eType);
			// Align!?
			// FIXME set __attribute__((packed)) for struct ph1dump!
			bb.getShort();
			long timeCreated = bb.getInt();
			Log.i("ipsec-tools", "time:" + timeCreated);
			int ph2Cnt = bb.getInt();
			Log.i("ipsec-tools", "ph2Cnt:" + ph2Cnt);
			
			
			pd.mItems.add(pd.new Item(status, side, remote, local,
				version, eType, timeCreated, ph2Cnt));
		}
		return pd;
	}
}
