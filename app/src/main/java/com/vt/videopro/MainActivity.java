package com.vt.videopro;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.vt.videopro.stickerUtils.FontProvider;
import com.vt.videopro.stickerUtils.ImageEntity;
import com.vt.videopro.stickerUtils.Layer;
import com.vt.videopro.stickerUtils.MotionEntity;
import com.vt.videopro.stickerUtils.MotionView;
import com.vt.videopro.stickerUtils.TextEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	public static final int SELECT_STICKER_REQUEST_CODE = 123;
	private final int[] stickerIds = { R.drawable.sticker_1 , R.drawable.sticker_3 , R.drawable.sticker_3 , R.drawable.sticker_4 , R.drawable.sticker_5 , R.drawable.sticker_6 , R.drawable.sticker_7 , R.drawable.sticker_8 , R.drawable.sticker_9 , R.drawable.sticker_10 , R.drawable.sticker_11 , R.drawable.sticker_12 };
	VideoView showVideo;
	MediaController controller;
	ImageView pickVideo, pickFrame;
	Uri uris;
	int current;
	TextView addText;
	EditText custumAddText;
	AlertDialog.Builder alertDialogBuilder;
	AlertDialog dialog;
	LayoutInflater inflater;
	Intent intent;
	MediaCodec decoder;
	MotionView motionView;
	RelativeLayout relative;
	ConstraintLayout fullView;
	LinearLayoutManager linearLayoutManager;
	GridLayoutManager gridLayoutManager;
	StickerAdapter stickerAdapter;
	View textEntityEditPanel;
	private final MotionView.MotionViewCallback motionViewCallback = new MotionView.MotionViewCallback ( ) {
		@Override
		public void onEntitySelected ( @Nullable MotionEntity entity ) {
			if ( entity instanceof TextEntity ) {
				textEntityEditPanel.setVisibility ( View.VISIBLE );
			}
			else {
				textEntityEditPanel.setVisibility ( View.GONE );
			}
		}

		@Override
		public void onEntityDoubleTap ( @NonNull MotionEntity entity ) {
//			startTextEntityEditing();
		}
	};
	RecyclerView stickerRecycler;
	TextView close;
	LinearLayout stickerLayout;
	ImageView emo;
	List < Integer > lists = new ArrayList <> ( );
	FFmpeg ffmpeg;
	private boolean weAreInterestedInThisTrack;
	private FontProvider fontProvider;
	ProgressBar progressBar;

	public static boolean isExternalStorageDocument ( Uri uri ) {
		return "com.android.externalstorage.documents".equals ( uri.getAuthority ( ) );
	}

	public static boolean isDownloadsDocument ( Uri uri ) {
		return "com.android.providers.downloads.documents".equals ( uri.getAuthority ( ) );
	}

	public static boolean isMediaDocument ( Uri uri ) {
		return "com.android.providers.media.documents".equals ( uri.getAuthority ( ) );
	}

	public static boolean isGooglePhotosUri ( Uri uri ) {
		return "com.google.android.apps.photos.content".equals ( uri.getAuthority ( ) );
	}

	public static String getDataColumn ( Context context , Uri uri , String selection , String[] selectionArgs ) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver ( ).query ( uri , projection , selection , selectionArgs , null );
			if ( cursor != null && cursor.moveToFirst ( ) ) {
				final int column_index = cursor.getColumnIndexOrThrow ( column );
				return cursor.getString ( column_index );
			}
		} finally {
			if ( cursor != null ) {
				cursor.close ( );
			}
		}
		return null;
	}

	@Override
	protected void onCreate ( Bundle savedInstanceState ) {
		super.onCreate ( savedInstanceState );
		setContentView ( R.layout.activity_main );

		showVideo = findViewById ( R.id.show_video );
		pickVideo = findViewById ( R.id.pick_video );
		pickFrame = findViewById ( R.id.get_frame );
		addText = findViewById ( R.id.add_text );

		motionView = findViewById ( R.id.motionview );
		relative = findViewById ( R.id.relative_view );
		fullView = findViewById ( R.id.linearLayout );

		stickerRecycler = findViewById ( R.id.show_stickers );
		stickerLayout = findViewById ( R.id.sticker_rec_view );
		ImageView emo = findViewById ( R.id.sticker_show );
		ImageView save = findViewById ( R.id.save );
		progressBar = findViewById ( R.id.progreess );

		emo.setOnClickListener ( new View.OnClickListener ( ) {
			@Override
			public void onClick ( View v ) {
				stickerLayout.animate ( ).translationY ( 0f ).setDuration ( 0 );
			}
		} );
		close = findViewById ( R.id.close );

		close.setOnClickListener ( new View.OnClickListener ( ) {
			@Override
			public void onClick ( View v ) {
				stickerLayout.animate ( ).translationY ( 1000f ).setDuration ( 0 );
			}
		} );


		gridLayoutManager = new GridLayoutManager ( this , 4 );

		for ( int i = 0 ; i < stickerIds.length ; i++ ) {
			lists.add ( stickerIds[ i ] );
		}

		stickerAdapter = new StickerAdapter ( this , lists );
		stickerRecycler.setAdapter ( stickerAdapter );


		pickVideo.setOnClickListener ( new View.OnClickListener ( ) {
			@Override
			public void onClick ( View v ) {
				Intent intent1 = new Intent ( );
				intent1.setAction ( Intent.ACTION_GET_CONTENT );
				intent1.setDataAndType ( MediaStore.Video.Media.EXTERNAL_CONTENT_URI , "video/*" );
				startActivityForResult ( intent1 , 2000 );

			}
		} );

		save.setOnClickListener ( new View.OnClickListener ( ) {
			@Override
			public void onClick ( View v ) {


				ffmpeg = FFmpeg.getInstance ( MainActivity.this );

				try {
					File basePath = new File ( Environment.getExternalStorageDirectory ( ) , MainActivity.this.getString ( R.string.app_name ) );

					if ( ! basePath.exists ( ) ) {
						basePath.mkdirs ( );
					}
					String videoOutput = Environment.getExternalStorageDirectory ( ).getAbsolutePath ( ) + "/Download/" + System.currentTimeMillis ( ) + "test_video.mp4";

					View view = relative;
					view.setDrawingCacheEnabled ( true );
					Bitmap bitmap = Bitmap.createBitmap ( view.getDrawingCache ( ) );
					view.setDrawingCacheEnabled ( false );

					String img_input = System.currentTimeMillis ( ) + "_text_img.png";
					File local = new File ( basePath , img_input );
					FileOutputStream ostream = new FileOutputStream ( local );
					bitmap.compress ( Bitmap.CompressFormat.PNG , 100 , ostream );
					ostream.flush ( );
					ostream.close ( );
//					Toast.makeText ( getApplicationContext ( ) , "saving : " + bitmap , Toast.LENGTH_SHORT ).show ( );
					ffmpeg.loadBinary ( new LoadBinaryResponseHandler ( ) );
					String[] cmd = new String[] { "-y" , "-i" , getPath ( MainActivity.this , uris ) , "-i" , local.getAbsolutePath ( ) , "-filter_complex" , "[1:v] scale=100:100 [ovrl]; [0:v][ovrl]overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2" , videoOutput };
					for ( int i = 0 ; i < cmd.length ; i++ ) {
						Log.d ( getClass ( ).getSimpleName ( ) , "command :" + cmd[ i ] );
					}

					ffmpeg.execute ( cmd , new ExecuteBinaryResponseHandler ( ) {
						@Override
						public void onSuccess ( String message ) {
							super.onSuccess ( message );
						}

						@Override
						public void onProgress ( String message ) {
							super.onProgress ( message );
						}

						@Override
						public void onFailure ( String message ) {
							super.onFailure ( message );
						}

						@Override
						public void onStart ( ) {
							super.onStart ( );
							progressBar.setVisibility ( View.VISIBLE );

						}

						@Override
						public void onFinish ( ) {
							super.onFinish ( );
							Toast.makeText ( MainActivity.this , "saved successfully" , Toast.LENGTH_SHORT ).show ( );
							progressBar.setVisibility ( View.GONE );
						}
					} );
				} catch ( Exception e ) {
					e.printStackTrace ( );
				}

			}
		} );

	}


	@Override
	protected void onActivityResult ( int requestCode , int resultCode , @Nullable Intent data ) {
		super.onActivityResult ( requestCode , resultCode , data );
		if ( requestCode == 2000 ) {
			if ( resultCode == RESULT_OK ) {

				uris = data.getData ( );
				showVideo.setVideoURI ( uris );
				showVideo.start ( );
				showVideo.setOnCompletionListener ( new MediaPlayer.OnCompletionListener ( ) {
					@Override
					public void onCompletion ( MediaPlayer mp ) {
						showVideo.start ( );
					}
				} );
//				extractVideoFrame ( getPath ( this , uris ) , this );
			}
		}
	}

	public void addSticker ( int stickerResId ) {
		Layer layer = new Layer ( );
		Bitmap pica = BitmapFactory.decodeResource ( getResources ( ) , stickerResId );

		ImageEntity entity = new ImageEntity ( layer , pica , motionView.getWidth ( ) , motionView.getHeight ( ) );

		motionView.addEntityAndPosition ( entity );
	}

	public String getPath ( final Context context , final Uri uri ) {

// DocumentProvider
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri ( context , uri ) ) {

			if ( isExternalStorageDocument ( uri ) ) {// ExternalStorageProvider
				final String docId = DocumentsContract.getDocumentId ( uri );
				final String[] split = docId.split ( ":" );
				final String type = split[ 0 ];
				String storageDefinition;


				if ( "primary".equalsIgnoreCase ( type ) ) {

					return Environment.getExternalStorageDirectory ( ) + "/" + split[ 1 ];

				}
				else {

					if ( Environment.isExternalStorageRemovable ( ) ) {
						storageDefinition = "EXTERNAL_STORAGE";

					}
					else {
						storageDefinition = "SECONDARY_STORAGE";
					}

					return System.getenv ( storageDefinition ) + "/" + split[ 1 ];
				}

			}
			else if ( isDownloadsDocument ( uri ) ) {// DownloadsProvider

				final String id = DocumentsContract.getDocumentId ( uri );
				final Uri contentUri = ContentUris.withAppendedId ( Uri.parse ( "content://downloads/public_downloads" ) , Long.valueOf ( id ) );

				return getDataColumn ( context , contentUri , null , null );

			}
			else if ( isMediaDocument ( uri ) ) {// MediaProvider
				final String docId = DocumentsContract.getDocumentId ( uri );
				final String[] split = docId.split ( ":" );
				final String type = split[ 0 ];

				Uri contentUri = null;
				if ( "image".equals ( type ) ) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				}
				else if ( "video".equals ( type ) ) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				}
				else if ( "audio".equals ( type ) ) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[ 1 ] };

				return getDataColumn ( context , contentUri , selection , selectionArgs );
			}

		}
		else if ( "content".equalsIgnoreCase ( uri.getScheme ( ) ) ) {// MediaStore (and general)

// Return the remote address
			if ( isGooglePhotosUri ( uri ) ) {
				return uri.getLastPathSegment ( );
			}

			return getDataColumn ( context , uri , null , null );

		}
		else if ( "file".equalsIgnoreCase ( uri.getScheme ( ) ) ) {// File
			return uri.getPath ( );
		}

		return null;
	}

	public class StickerAdapter extends RecyclerView.Adapter < StickerAdapter.StickerHolder > {

		Context context;
		List < Integer > stringList;

		public StickerAdapter ( Context context , List < Integer > stringList ) {
			this.context = context;
			this.stringList = stringList;
		}

		@NonNull
		@Override
		public StickerAdapter.StickerHolder onCreateViewHolder ( @NonNull ViewGroup viewGroup , int i ) {
			LayoutInflater inflater = LayoutInflater.from ( context );
			View view = inflater.inflate ( R.layout.custom_show_stickers , viewGroup , false );
			return new StickerAdapter.StickerHolder ( view );
		}

		@Override
		public void onBindViewHolder ( @NonNull StickerAdapter.StickerHolder stickerHolder , final int i ) {

			stickerHolder.stickers.setImageResource ( stringList.get ( i ) );
			stickerHolder.stickers.setOnClickListener ( new View.OnClickListener ( ) {
				@Override
				public void onClick ( View v ) {
					addSticker ( stringList.get ( i ) );
				}
			} );
		}

		@Override
		public int getItemCount ( ) {
			return stringList.size ( );
		}

		public class StickerHolder extends RecyclerView.ViewHolder {

			ImageView stickers;

			public StickerHolder ( @NonNull View itemView ) {
				super ( itemView );
				stickers = itemView.findViewById ( R.id.image_sticker );
			}
		}
	}
}